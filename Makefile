IMAGE_NAME:=trodix/clipystream

default:
	cat ./Makefile

image:
	mvn clean package -DskipTests && docker build -t $(IMAGE_NAME) ./backend

start:
	docker-compose --profile prod up

stop:
	docker-compose down

dev-backend:
	docker-compose --profile dev up --remove-orphans && cd ./backend && mvn clean install -DskipTests spring-boot:run

test:
	mvn clean test

verify:
	mvn clean verify

package:
	mvn clean package

dev-frontend:
	cd ./frontend && ./node_modules/.bin/ng serve

test-frontend:
	cd ./frontend && ./node_modules/.bin/ng test

