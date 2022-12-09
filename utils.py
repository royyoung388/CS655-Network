from packet import Packet


def checksum(data: str):
    res = 0
    for b in str.encode(data):
        res += b
        res &= 0xFFF

    return ~res


def validateChecksum(packet: Packet):
    return ~checksum(packet.data) + packet.checksum == -1


if __name__ == '__main__':
    c = checksum('aaaaaaaaaaaaaaahhhhhhhhhhhaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa')
    print('{}'.format(c))
    r = ~checksum('aaaaaaaaaaaaaaahhhhhhhhhhhaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaz11     Q1') + c
    print(r)