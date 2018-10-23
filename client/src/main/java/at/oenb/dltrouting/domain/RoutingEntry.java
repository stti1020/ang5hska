package at.oenb.dltrouting.domain;

import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RoutingEntry {

	private String id;
	private BankOperation bankOperation;
	private Service service;
	private BIC counterPartyBic;
	private OffsetDateTime validFrom;
	private OffsetDateTime validTo;
	private BIC cooperationBic;

}
