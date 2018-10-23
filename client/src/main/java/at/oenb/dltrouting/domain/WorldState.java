package at.oenb.dltrouting.domain;

import java.util.List;

import at.oenb.dltrouting.domain.Bank.BankBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class WorldState {
	
	private List<Bank> bankList;

}
