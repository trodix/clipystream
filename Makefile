IMAGE_NAME:=trodix/clipystream

default:
	cat ./Makefile

image:
	mvn clean package -DskipTests && docker build -t $(IMAGE_NAME) ./backend

start:
	docker-compose up

stop:
	docker-compose down

dev-backend:
	cd ./backend && mvn clean install -DskipTests spring-boot:run

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

