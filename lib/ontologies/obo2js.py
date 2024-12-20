import re
import sys

def parse_obo(file_path):
    """
    Parses an OBO file to extract terms with their IDs, names, and definitions.
    """
    terms = {}
    with open(file_path, 'r') as obo_file:
        term = {}
        for line in obo_file:
            line = line.strip()
            if line == '[Term]':
                if 'id' in term and 'name' in term:
                    if 'def' in term:
                        term['def'] = format_definition(term['def'])
                    terms[term['id']] = {
                        'name': term['name'],
                        'def': term.get('def', '')
                    }
                term = {}
            elif line.startswith('id: '):
                term['id'] = line[4:]
            elif line.startswith('name: '):
                term['name'] = line[6:]
            elif line.startswith('def: '):
                term['def'] = line[5:].strip()
        # Add the last term in the file if it's valid
        if 'id' in term and 'name' in term:
            if 'def' in term:
                term['def'] = format_definition(term['def'])
                terms[term['id']] = {
                    'name': term['name'],
                    'def': term.get('def', '')
                }
    return terms

def format_definition(def_text):
    """
    Reformats the definition text to match the required format.
    Removes enclosing double quotes from the definition if present.
    """
    if def_text.startswith('"') and def_text.endswith('"'):
        def_text = def_text[1:-1]
    parts = def_text.rsplit('[', 1)
    if len(parts) == 2:
        definition, source = parts
        if definition.startswith('"') and definition.startswith('"'):
            definition = definition[1:-2]
        return f"{definition.strip()} [{source.strip()}"
    return def_text.strip()

def generate_js(terms, output_file):
    """
    Generates a JavaScript file exporting the terms as a module.
    """
    with open(output_file, 'w') as js_file:
        js_file.write("module.exports = {\n")
        for term_id, details in terms.items():
            js_file.write(f"  '{term_id}': {{\n")
            js_file.write(f"    'name': '{details['name']}'")
            if details['def']:
                if "'" in details['def']:
                    js_file.write(f",\n    'def': \"{details['def']}\"\n")
                else:
                    js_file.write(f",\n    'def': '{details['def']}'\n")
            else:
                js_file.write(f"\n")
            js_file.write("  },\n")
        js_file.write("};\n")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python obo_to_js.py <input_obo_file> <output_js_file>")
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]

    terms = parse_obo(input_file)
    generate_js(terms, output_file)
    print(f"JavaScript file '{output_file}' has been created successfully.")
