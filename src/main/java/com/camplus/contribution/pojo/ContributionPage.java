package com.camplus.contribution.pojo;

import java.util.List;

public record ContributionPage(
        List<UserContribution> items,
        int page,
        int pageSize,
        int total,
        int totalPages) {
}
