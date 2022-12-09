import base64
import socket
import threading

import utils
from packet import Packet


class ClientSocket:
    def __init__(self):
        self.worker_list = []
        self.worker_index = 0
        self.mutex = threading.Lock()

    def connectServer(self, ip, port):
        server = socket.socket()
        server.connect((ip, port))
        print(
            u'Client socket is connected with Server socket [ TCP_SERVER_IP: ' + ip + ', TCP_SERVER_PORT: '
            + str(port) + ' ]')
        return server

    def sendImages(self, imgBytes):
        while len(self.worker_list) > 0:
            ip = None
            port = None
            try:
                self.mutex.acquire()
                ip, port = self.worker_list[self.worker_index]
                self.worker_index = (self.worker_index + 1) % len(self.worker_list)
                self.mutex.release()

                print('Using worker: %s %s' % (ip, port))
                server = self.connectServer(ip, port)
                break
            except Exception as e:
                self.mutex.acquire()
                if (ip, port) in self.worker_list:
                    self.worker_list.remove((ip, port))
                    print('Inactive worker: %s %s' % (ip, port))
                    self.worker_index = self.worker_index % len(self.worker_list) if len(self.worker_list) > 0 else 0
                self.mutex.release()
        else:
            raise UserWarning("Error: No active worker!")

        result = []

        # img_str = base64.b64encode(open(filePath, 'rb').read()).decode('ascii')
        img_str = base64.b64encode(imgBytes).decode('ascii')

        # img = Image.open(filePath, mode='r')
        #
        # img_byte_arr = io.BytesIO()
        # img.save(img_byte_arr, 'png')
        # img_byte_arr = img_byte_arr.getvalue()

        # send image size
        while True:
            packetSize = Packet(Packet.DATA)
            packetSize.data = str(len(img_str))
            packetSize.checksum = utils.checksum(packetSize.data)
            server.sendall(bytes(packetSize))

            packet = Packet(server.recv(1024).decode('utf-8'))
            if packet.type != Packet.ACK:
                # resend size
                print('Received size ACK error: ' + str(packet))
                server.sendall(bytes(packetSize))
                continue
            print('Received size ACK')
            break

        # send image data
        while True:
            packetImg = Packet(Packet.DATA)
            packetImg.data = img_str
            packetImg.checksum = utils.checksum(packetImg.data)
            server.sendall(bytes(packetImg))

            packet = Packet(server.recv(1024).decode('utf-8'))
            if packet.type != Packet.ACK:
                # resend size
                print('Received image ACK error: ' + str(packet))
                server.sendall(bytes(packetImg))
                continue
            print('Received image data ACK')
            break

        # send result ACK
        while True:
            packet = Packet(server.recv(1024).decode('utf-8'))
            if packet.type != Packet.DATA and utils.validateChecksum(packet):
                # send NACK to resend result
                print('Received result error: ' + str(packet))
                server.sendall(bytes(Packet(Packet.NACK)))
                continue
            result = []
            for item in packet.data.split(','):
                result.append(item.split(':'))

            server.sendall(bytes(Packet(Packet.ACK)))
            break

        print('Image recognition result: ' + str(result))
        return result

    def add_worker(self, ip, port):
        self.worker_list.append((ip, int(port)))

    def del_worker(self, ip, port):
        self.worker_list.remove((ip, int(port)))


def test(client):
    try:
        client.sendImages(open('test/dog.jpg', 'rb').read())
    except Exception as e:
        print(e)


if __name__ == "__main__":
    client = ClientSocket()
    client.add_worker("localhost", "8888")
    client.add_worker("localhost", "7777")
    client.add_worker("localhost", "6666")

    for i in range(5):
        threading.Thread(target=test, args=(client,)).start()
