ACCOUNT=change-me
SCHEMA=./schemas/employee.json

login:
	decodable login

streams:
	jinja -d $(SCHEMA) -o model.sh templates/model.sh.j2
	chmod +x model.sh
	./model.sh
	decodable stream list -o json | jq -sr '.[] |select(.name=="Employee")|.id ' > .tmp

clean:
	rm -f ./model.sh
	decodable stream delete $(cat .tmp)
