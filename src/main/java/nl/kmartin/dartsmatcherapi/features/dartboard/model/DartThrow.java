package nl.kmartin.dartsmatcherapi.features.dartboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DartThrow {
    private Dart target;
    private Dart result;
}