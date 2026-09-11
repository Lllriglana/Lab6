# Лабораторная работа №5 — вариант 440

Console application for managing a collection of SpaceMarine objects in interactive mode. Data is loaded from a JSON file at startup and can be saved back with the `save` command.

## Requirements

- Java SE 17+.
- Коллекция: `java.util.LinkedList`.
- Чтение: `InputStreamReader`; запись: `BufferedOutputStream`; формат: JSON.

## Build

```bash
javac -d bin $(find src -name '*.java')
```

## Run

```bash
java -cp bin ITMO.Lab5.Main lab5_data.json
```

`lab5_data.json` is the data file path. You can use a relative path or an absolute path.

## Commands

- `help` - print command list
- `info` - print collection info
- `show` - list all elements
- `add` - add a new element (interactive input)
- `update <id>` - update element by id
- `remove_by_id <id>` - remove element by id
- `clear` - clear the collection
- `save` - save to the startup file path
- `execute_script <file>` - execute commands from a script file
- `exit` - terminate without saving
- `add_if_min` - add element if it is less than current minimum
- `shuffle` - shuffle elements randomly
- `history` - print last 11 commands
- `count_less_than_weapon_type <weaponType>` - count elements with weaponType less than provided
- `filter_by_weapon_type <weaponType>` - list elements with the given weaponType
- `print_unique_category` - print unique category values

Полная автономная демонстрация всех возможностей проекта:

```text
execute_script scripts/full_demo.txt
```

Сценарий намеренно содержит несколько некорректных значений, чтобы показать повторный ввод и валидацию. Для безопасности сначала используйте копию JSON-файла: команда `clear` очищает коллекцию, а `save` сохраняет демонстрационные данные.

## Interactive Input Rules

- `id` and `creationDate` are generated automatically and are not entered by the user.
- Enum fields accept one of the allowed constants (case-insensitive).
- Empty line is treated as `null` for nullable fields.
- On invalid input, the field is requested again.

## JSON Format

Root is an array of objects. Example:

```json
[
	{
		"id": 1,
		"name": "Alpha",
		"coordinates": {"x": 10.5, "y": 20.0},
		"creationDate": "2026-05-24",
		"health": 100.5,
		"category": "SCOUT",
		"weaponType": "MELTAGUN",
		"meleeWeapon": "POWER_FIST",
		"chapter": {"name": "Ultramar", "marinesCount": 1000}
	}
]
```

## Javadoc

Generate documentation into [docs/](docs/):

```bash
javadoc -d docs $(find src -name '*.java')
```

Для всех классов исходного кода предусмотрены комментарии Javadoc. Откройте [docs/index.html](docs/index.html) в браузере.

## Project Structure

- [src/](src/) - sources
- [bin/](bin/) - compiled classes
- [docs/](docs/) - Javadoc output
- [lab5_data.json](lab5_data.json) - default data file
