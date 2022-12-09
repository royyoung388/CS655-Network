import base64
import socket
import sys
import threading
from io import BytesIO

import torch
from PIL import Image
from torchvision import transforms

import utils
from packet import Packet


class ServerSocket:
    def __init__(self, ip, port):
        self.sock = None
        self.model = None
        self.categories = None

        self.TCP_IP = ip
        self.TCP_PORT = port
        self.loadModel()
        self.socketOpen()

    def loadModel(self):
        self.model = torch.hub.load('pytorch/vision:v0.10.0', 'squeezenet1_1', pretrained=True)
        self.model.eval()

        # Read the categories
        with open("imagenet_classes.txt", "r") as f:
            self.categories = [s.strip() for s in f.readlines()]

    def socketClose(self):
        self.sock.close()
        print(u'Server socket [ TCP_IP: ' + self.TCP_IP + ', TCP_PORT: ' + str(self.TCP_PORT) + ' ] is close')

    def socketOpen(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.bind((self.TCP_IP, self.TCP_PORT))
        self.sock.listen(1)
        print(u'Server socket [ TCP_IP: ' + self.TCP_IP + ', TCP_PORT: ' + str(self.TCP_PORT) + ' ] is open')
        while True:
            client, addr = self.sock.accept()
            print(u'Server socket [ TCP_IP: ' + self.TCP_IP + ', TCP_PORT: ' + str(
                self.TCP_PORT) + ' ] is connected with client')
            threading.Thread(target=self.receive, args=(client,)).start()

    def receive(self, client: socket.socket):
        size = 0

        # receive image size
        while True:
            packet = Packet(client.recv(1024).decode('utf-8'))
            if packet.type != Packet.DATA or not utils.validateChecksum(packet):
                # NACK
                print('Received size packet error: ' + str(packet))
                client.sendall(bytes(Packet(Packet.NACK)))
                continue
            # ACK
            client.sendall(bytes(Packet(Packet.ACK)))
            # extra 8 bytes for TYPE, CHECKSUM and SPACE
            size = int(packet.data) + 8
            print('Received image size: ' + str(size))
            break

        # receive image
        while True:
            length = 0
            data = b''
            while length < size:
                d = client.recv(1024)
                data += d
                length += len(d)
            print('Received data packet size: %d' % len(data))
            packetImg = Packet(data.decode('utf-8'))
            # validate packet
            if not utils.validateChecksum(packetImg):
                # NACK
                print('Received image packet error: ' + str(packetImg))
                client.sendall(bytes(Packet(Packet.NACK)))
                continue
            # ACK
            client.sendall(bytes(Packet(Packet.ACK)))
            break

        # image recognition
        result = self.recognition(packetImg.data)
        # result packet
        packetRes = Packet(Packet.DATA)
        packetRes.data = ','.join(result)
        packetRes.checksum = utils.checksum(packetRes.data)
        # send result
        client.sendall(bytes(packetRes))

        # receive result ack
        while True:
            packet = Packet(client.recv(1024).decode('utf-8'))
            if packet.type != Packet.ACK:
                # resend result
                print('Received result ACK error: ' + str(packet))
                client.sendall(bytes(packetRes))
                continue
            break

    # image recognition
    def recognition(self, data):
        input_image = Image.open(BytesIO(base64.b64decode(data)))
        # input_image.save('setting1.png', format='png')

        preprocess = transforms.Compose([
            transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
        ])
        input_tensor = preprocess(input_image)
        input_batch = input_tensor.unsqueeze(0)  # create a mini-batch as expected by the model

        # move the input and model to GPU for speed if available
        if torch.cuda.is_available():
            input_batch = input_batch.to('cuda')
            self.model.to('cuda')

        with torch.no_grad():
            output = self.model(input_batch)
        # Tensor of shape 1000, with confidence scores over Imagenet's 1000 classes
        # The output has unnormalized scores. To get probabilities, you can run a softmax on it.
        probabilities = torch.nn.functional.softmax(output[0], dim=0)

        # Show top categories per image
        top5_prob, top5_catid = torch.topk(probabilities, 5)

        result = []
        for i in range(top5_prob.size(0)):
            result.append(self.categories[top5_catid[i]] + ":" + str(top5_prob[i].item()))
        print('model top 5 result: ' + str(result))

        return result


if __name__ == "__main__":
    IP = sys.argv[1]
    PORT = int(sys.argv[2])
    server = ServerSocket(IP, PORT)
