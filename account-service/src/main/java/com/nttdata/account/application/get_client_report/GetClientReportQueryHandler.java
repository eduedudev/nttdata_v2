package com.nttdata.account.application.get_client_report;

import reactor.core.publisher.Flux;

public interface GetClientReportQueryHandler {

    Flux<AccountMovementReport> handle(GetClientReportQuery query);
}
