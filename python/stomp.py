import io
import logging

logger = logging.getLogger(__name__)
handler = logging.StreamHandler()
handler.setFormatter(logging.Formatter('%(asctime)s:%(name)s:%(levelname)s:%(message)s'))
logger.addHandler(handler)
logger.setLevel(logging.DEBUG)


class StompFrame:
    def __init__(self):
        self.headers = {}
        self.command = None
        self.content = None

    def get_command(self):
        return self.command

    def set_command(self, command):
        self.command = command

    def get_content(self):
        return self.content

    def set_content(self, content):
        self.content = content

    def get_header(self, key):
        return self.headers[key]

    def set_header(self, key, value):
        self.headers[key] = value

    def write(self, out):
        logger.debug('write')
        out.write(self.command)
        out.write('\n')
        for key in self.headers:
            out.write(key)
            out.write(':')
            out.write(self.headers[key])
            out.write('\n')
        out.write('\n')
        if self.content is not None:
            out.write(self.content)
        out.write('\0')

    @staticmethod
    def parse(input):
        logger.debug('parse')
        frame = StompFrame()
        frame.command = input.readline().rstrip('\r\n')
        for line in input:
            line = line.rstrip('\r\n')
            if line == '':
                break
            (name, value) = line.split(':')
            frame.headers[name] = value
        frame.content = input.read()[:-1]
        logger.debug('parse frame content: %s', frame.content)
        return frame
