package ru.sber.df.epmp.netty_postgres.exception;

import static java.lang.String.format;

public class SqlNotAllowedException extends RuntimeException{
    public SqlNotAllowedException(String envError){
        super(format("%s User has no permitions..",envError));
    }
}
