package ru.sbt.jschool.session6;

public enum Status {
    OK{
        @Override
        public String toString() {
            return "200 OK";
        }
    },
    NOT_FOUND{
        @Override
        public String toString() {
            return "404 Not Found";
        }
    }
}
