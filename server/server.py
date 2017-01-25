import json
import logging
import config
from subprocess import Popen, PIPE
from datetime import datetime

import requests

from flask import Flask, Response, jsonify, redirect, request, url_for
from flask_login import *
from pymongo import MongoClient
from werkzeug.utils import secure_filename


# Flask configuration
app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Setup logging
logging.basicConfig(filename='debug.log', level=logging.INFO)

# Setup a DB
database = "localhost:27017"
client = MongoClient(database)
db = client.beta
threads = []


@app.route('/api/gallery', methods=['POST', 'GET'])
def filter_gallery():
    content = request.get_json()
    query = json.loads(content['text'])
    result = feedback(query)
    resp = Response(
        response=js.dumps(result), status=200, mimetype="application/json")
    return resp


def feedback(query):
    p = Popen(
        ['python3', '../budi_ml/feedback_nlp/main.py', query], stdout=PIPE, stderr=PIPE)
    output, err = p.communicate()
    return output[0]


if __name__ == "__main__":
    # Run this with python3 server.py and then tail -f mvp.log
    logging.info("Began running at {0}".format(datetime.now()))
    logging.info(" ")
    app.run(host='0.0.0.0', port=80)
