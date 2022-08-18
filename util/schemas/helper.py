#!/bin/local/python

def handle_dict(key:str=None, value=None, parent=None):
	for key in value.keys():
		switcher.get(type(value[key]), handle_field)(key, value[key])
def handle_list(key:str=None, value=None, parent=None):
	print()
def handle_field(key:str=None, value=None, parent=None):
	if(parent == None):
		print("--field {}={}".format(key, type(value).__name__))
	else:
		print("`{}` {}".format(key, type(value).__name__))

switcher = {
	dict: handle_dict,
	list: handle_list
}


def main():
	load_dotenv()
	logging.basicConfig(level=logging.INFO)

	msg = {}
	func = switcher.get(type(msg), handle_field)
	print(func)
	func(value=msg)



if __name__== "__main__":
	main()