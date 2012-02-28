import datetime
# class Log(object):
# 	def __init__(self, application=None):
# 		print "Logger initialized"
	
def info(message):
	now = datetime.datetime.now()
	print "[%s] - %s" % ( str(now), message)