#!/usr/bin/env python
# encoding: utf-8
"""
wsgi.py
"""

import sys
import os
# external
# from werkzeug.wrappers import Request, Response
# pycket
# from pycket.application import webapplication
# from pycket.request import process_request


class WSGIHandler(object):
    def __init__(self, application=None):
        # if application :
        #             webapplication.init(application)
		app.run(host='127.0.0.1', port=3031, debug=None )
    
    # def __call__(self, environ, start_response):
    #     request = Request(environ)
    #     response = process_request(request)
    #     return response(environ,start_response)

