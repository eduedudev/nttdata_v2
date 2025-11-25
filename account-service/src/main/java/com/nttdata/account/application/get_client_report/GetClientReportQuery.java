package com.nttdata.account.application.get_client_report;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class GetClientReportQuery {

    private final Long clientId;
    private final OffsetDateTime startDate;
    private final OffsetDateTime endDate;
}
