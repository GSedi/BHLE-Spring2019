from flask import Flask
import requests
import json
# from flask_sslify import SSLify

# app = Flask(__name__)
# sslify = SSLify(app)

# @app.route('/')
# def index():
#     return '<h1>Hello world</h1>'

URL = "https://api.telegram.org/bot729627655:AAF-ib5gaoCtx-gmJplVnfyOT0Zgob5_DEI/"

def write_json(data, filename='answer.json'):
    with open(filename, 'w') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

def get_updates():
    url = URL + 'getUpdates'
    r = requests.get(url)
    # write_json(r.json())
    return r.json()

def send_message(chat_id, text='asdasdasd'):
    url = URL + 'sendMessage'
    answer = {
        'chat_id': chat_id,
        'text': text
    }
    r = requests.post(url, json=answer)
    return r.json()


def main():
    # r = requests.get(URL + 'getMe')
    # print(r.json())
    # write_json(r.json())
    # get_updates()
    r = get_updates()
    chat_id = r['result'][-1]['message']['chat']['id']
    send_message(chat_id)

if __name__ == "__main__":
    # app.run()
    main()