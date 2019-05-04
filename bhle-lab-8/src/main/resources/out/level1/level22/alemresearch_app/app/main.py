from flask import Flask
from flask import request
from flask import jsonify
import requests
import json
from flask_sslify import SSLify
import telegram
import telebot

TOKEN = '729627655:AAF-ib5gaoCtx-gmJplVnfyOT0Zgob5_DEI'
URL = "https://api.telegram.org/bot729627655:AAF-ib5gaoCtx-gmJplVnfyOT0Zgob5_DEI/"

app = Flask(__name__)

bot = telebot.TeleBot(TOKEN)

def write_json(data, filename='answer.json'):
    with open(filename, 'w') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)


def send_message(chat_id, text='asdasdasd'):
    url = URL + 'sendMessage'
    answer = {
        'chat_id': chat_id,
        'text': text
    }
    r = requests.post(url, json=answer)
    return r.json()

# https://api.telegram.org/bot729627655:AAF-ib5gaoCtx-gmJplVnfyOT0Zgob5_DEI/setWebhook?url=https://1ce93625.ngrok.io/

# @app.route('/', methods= ['POST', 'GET'])
# def index():
#     if request.method == 'POST':
#         r = request.get_json()
#         # write_json(r)
#         chat_id = r['message']['chat']['id']
#         message = r['message']['text']

#         # send_message(chat_id)

#         return jsonify(r)
#     return '<h1>Hello bot</h1>'

@bot.message_handler(commands=['start', 'help'])
def startCommand(message):
    # bot.send_message(message.chat.id, 'Hi *' + message.chat.first_name + '*!' , parse_mode='Markdown', reply_markup=types.ReplyKeyboardRemove())
    write_json(message)

if __name__ == "__main__":
    app.run()