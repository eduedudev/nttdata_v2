package com.nttdata.account.application.get_client_report;

import java.time.OffsetDateTime;

public class GetClientReportQueryMother {

    private static final OffsetDateTime DEFAULT_START = OffsetDateTime.parse("2025-11-01T00:00:00Z");
    private static final OffsetDateTime DEFAULT_END = OffsetDateTime.parse("2025-11-30T23:59:59Z");

    public static GetClientReportQuery.GetClientReportQueryBuilder validQuery() {
        return GetClientReportQuery.builder()
                .clientId(1L)
                .startDate(DEFAULT_START)
                .endDate(DEFAULT_END);
    }

    public static GetClientReportQuery createDefault() {
        return validQuery().build();
    }

    public static GetClientReportQuery createWithClientId(Long clientId) {
        return validQuery().clientId(clientId).build();
    }

    public static GetClientReportQuery createWithDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        return validQuery()
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public static GetClientReportQuery createForLastWeek() {
        OffsetDateTime now = OffsetDateTime.now();
        return validQuery()
                .startDate(now.minusWeeks(1))
                .endDate(now)
                .build();
    }
}
