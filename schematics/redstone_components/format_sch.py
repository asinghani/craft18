from sexpdata import loads, dumps, Symbol

with open('redstone_components.kicad_sch', 'r') as f:
	data_str = f.read()

root = loads(data_str)

def get_named_fields(sexpr, name):
	for arg in sexpr:
		if isinstance(arg, list):
			if arg[0] == Symbol(name):
				yield arg

def get_named_field(sexpr, name):
	fields = list(get_named_fields(sexpr, name))
	if len(fields) != 1:
		raise Exception()
	return fields[0]
			
for symbol in get_named_fields(root, 'symbol'):
	lib_id = get_named_field(symbol, 'lib_id')[1]
	if lib_id == 'power:GND':
		print(symbol)
		print()