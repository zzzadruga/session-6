package ru.sbt.jschool.session6;

public enum ContentType {
    JSON{
        @Override
        public String toString() {
            return "application/json";
        }
    },
    HTML{
        @Override
        public String toString() {
            return "text/html";
        }
    }
}
