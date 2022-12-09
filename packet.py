class Packet:
    ACK = 'A'
    NACK = 'N'
    DATA = 'D'

    def __init__(self, message:str):
        self.type = message[0]
        self.data = ''
        self.checksum = 0

        if len(message) > 1:
            self.data = message[2:-6]
            self.checksum = int(message[-5:])

        # msg = message.split()
        # self.type = msg[0]
        #
        # if len(msg) == 3:
        #     self.data = msg[1]
        #     self.checksum = int(msg[2])

    def __bytes__(self):
        return bytes(str(self), 'utf-8')
        # if self.type == Packet.DATA:
        #     return bytes(self.type, 'utf-8') + b' ' + bytes(self.data, 'utf-8') + b' ' + bytes(self.checksum, 'utf-8')
        # else:
        #     return bytes(self.type)

    def __str__(self):
        if self.type == Packet.DATA:
            return "%s %s %05d" % (self.type, str(self.data), self.checksum)
        else:
            return self.type