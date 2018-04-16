import json
import argparse
import urllib.request as req

if __name__ == '__main__':
#================================
# Parse Command Line Args
#================================
	parser = argparse.ArgumentParser()
	parser.add_argument('-f','--filename', help='Enter source filename.')
	args = parser.parse_args()
	
	data = json.load(open(args.filename,encoding='UTF-8'))
	
	card = data[0]
	
	# for card in data:
	if card['set'] == 'Core Set':
		imgurl = card['img']
		print ("Getting: " + imgurl)
		filename = imgurl.split('/')[-1]
		req.urlretrieve(imgurl, '1_downloads/' + filename)
		if card['type'] == 'Warlord Unit':
			imgurlb = imgurl.split('.')[0] + 'b.jpg'
			filenameb = imgurlb.split('/')[-1]
			req.urlretrieve(imgurlb, '1_downloads/' + filenameb)