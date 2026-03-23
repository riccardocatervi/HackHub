package hackhub.builder;

import hackhub.model.entity.Invito;
import hackhub.model.entity.Team;

import java.util.Collections;
import java.util.List;

/**
 * Oggetto risultato prodotto da {@link EquipeBuilder#build()}.
 * Incapsula il {@link Team} appena costruito e la lista degli
 * {@link Invito} da persistere e notificare.
 */
public class EquipeCreationResult {

    private final Team team;
    private final List<Invito> inviti;

    public EquipeCreationResult(Team team, List<Invito> inviti) {
        this.team = team;
        this.inviti = Collections.unmodifiableList(inviti);
    }

    public Team getTeam() {
        return team;
    }

    public List<Invito> getInviti() {
        return inviti;
    }
}
