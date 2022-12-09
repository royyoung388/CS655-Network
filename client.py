import sys

from flask import Flask, render_template, request

from clientsocket import ClientSocket

app = Flask(__name__)

client = None


@app.route('/', methods=['GET', 'POST'])
def upload():
    global client
    if request.method == 'POST':
        f = request.files['file']
        print(request.files)
        if not f.filename:
            print('No file')
            return render_template('index.html')
        try:
            result = client.sendImages(f.stream.read())
            return render_template('index.html', result=result)
        except UserWarning as e:
            return render_template('info.html', info=str(e))

    else:
        return render_template('index.html')


@app.route('/add/<ip>:<port>')
def add_worker(ip, port):
    global client
    print('add worker: %s %s' % (ip, port))
    client.add_worker(ip, port)
    return render_template('info.html', info='Add worker successful')


@app.route('/del/<ip>:<port>')
def del_worker(ip, port):
    global client
    print('delete worker: %s %s' % (ip, port))
    client.del_worker(ip, port)
    return render_template('info.html', info='Delete worker successful')


if __name__ == '__main__':
    client = ClientSocket()
    app.run(debug=False, host="0.0.0.0", port=9999)
