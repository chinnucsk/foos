from setuptools import setup, find_packages
import sys, os

version = '0.3'

setup(name='foosball',
      version=version,
      description="foosball stats",
      long_description="""\
""",
      classifiers=[], # Get strings from http://pypi.python.org/pypi?%3Aaction=list_classifiers
      keywords='',
      author='pablazo',
      author_email='',
      url='',
      license='',
      packages=find_packages(exclude=['ez_setup', 'examples', 'tests']),
      include_package_data=True,
      zip_safe=False,
      install_requires=[
          # -*- Extra requirements: -*-
          'Flask==0.8',
          'pymongo==2.0.1',
		  'psycopg2==2.2.2',
		  'SQLAlchemy==0.7.4',
		  # 'flask-lesscss==0.9.1', # needs the LessCSS gem installed to use this and compile in the server. Otherwise, using the client side usage.
		  # 'python-fedora==0.3.20',
		  # 'TurboJson==1.3.2',
		  # 'Flask-SQLAlchemy==0.15',
		  # 'gevent==0.13.6',
      ],
      entry_points="""
      # -*- Entry points: -*-
      """
	  ,
      )
