package at.oenb.dltrouting.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BIC {

    String value;

}
