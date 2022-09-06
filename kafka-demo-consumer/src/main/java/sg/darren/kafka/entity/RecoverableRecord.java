package sg.darren.kafka.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity(name = "recoverable_record")
public class RecoverableRecord {

    @Id
    @GeneratedValue
    private Long id;

    private String topic;

    private Long key;

    private String value;

    private Integer partition;

    @Column(name = "offset")
    private Long offset;

    private String exception;

    @Enumerated(EnumType.STRING)
    private RecoverableStatus status;

}
