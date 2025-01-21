package ru.sber.df.epmp.netty_postgres.server.postgres.tcp.domain;

public sealed interface IFrontendMessage permits FrontendCommandMessage, FrontendBootstrapMessage {
}
