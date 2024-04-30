def search_tag(json_file: dict, tag: str) -> bool:
    find = False
    for key, value in json_file.items():
        if key == tag: return True
        if isinstance(value, dict):
            find = search_tag(value, tag)
        elif isinstance(value, list):
            for element in value:
                #check for completeness
                if isinstance(element, dict):
                    find = search_tag(element, tag)
                    if find: break
        if find: break
    return find

def search_value(json_file: dict, tag: str, value) -> bool:
    find = False
    for dict_key, dict_value in json_file.items():
        if dict_key == tag and dict_value == value: return True
        if isinstance(dict_value, dict):
            find = search_value(dict_value, tag, value)
        elif isinstance(dict_value, list):
            for element in dict_value:
                #check for completeness
                if isinstance(element, dict):
                    find = search_value(element, tag, value)
                    if find: break
                elif isinstance(element, str):
                    if element == value:
                       find = True
                       if find: break
        if find: break
    return find

def get_value_from_key(json_file: dict, tag:str) -> bool:
    find_value = None
    for key, value in json_file.items():
        if key == tag: return value
        if isinstance(value, dict):
            find_value = get_value_from_key(value, tag)
        elif isinstance(value, list):
            for element in value:
                #check for completeness
                if isinstance(element, dict):
                    find_value = get_value_from_key(element, tag)
                    if find_value: break
        if find_value: break
    return find_value

def convert_json_values_toString(json_file: dict) -> dict:
  for key, value in json_file.items():
    if isinstance(value, int) or isinstance(value, float): json_file[key] = str(value)
    elif isinstance(value, dict): convert_json_values_toString(value)
    elif isinstance(value, list):
      for element in value:
        #check for completeness
        if isinstance(element, dict):
          convert_json_values_toString(element)  
  return json_file
