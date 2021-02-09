package me.m1dnightninja.midnightcore.fabric.api;

import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.util.TextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.ArrayList;
import java.util.List;

public class CustomScoreboard {

    private final List<ServerPlayer> players = new ArrayList<>();

    private final PlayerTeam[] teams = new PlayerTeam[15];
    private final ServerScoreboard board;
    private final Objective objective;

    private final boolean[] updated = new boolean[15];

    private Component name;

    public CustomScoreboard(String id, Component title) {

        board = MidnightCore.getServer().getScoreboard();
        objective = new Objective(board, id, ObjectiveCriteria.DUMMY, title, ObjectiveCriteria.RenderType.INTEGER);

        for(int i = 0 ; i < teams.length ; i++) {

            if(id.length() > 15) {
                id = id.substring(0,14);
            }

            teams[i] = new PlayerTeam(board, id + Integer.toHexString(i));
            teams[i].getPlayers().add("§" + Integer.toHexString(i));
        }
    }

    public void setName(Component cmp) {
        objective.setDisplayName(cmp);
    }

    public void setLine(int line, Component message) {

        if(line < 1 || line > 15) return;

        if(message == null) {
            teams[line].setPlayerPrefix(null);
            board.resetPlayerScore("§" + Integer.toHexString(line), objective);
        } else {
            board.getOrCreatePlayerScore("§" + Integer.toHexString(line), objective).setScore(line);


            teams[line].setPlayerPrefix(message);
        }

        updated[line] = true;
    }

    public void addPlayer(ServerPlayer player) {

        players.add(player);

        player.connection.send(new ClientboundSetObjectivePacket(objective, 0));

        for(int i = 0 ; i < teams.length ; i++) {
            player.connection.send(new ClientboundSetPlayerTeamPacket(teams[i], 0));

            String name = "§" + Integer.toHexString(i);
            Score s = board.getOrCreatePlayerScore(name, objective);
            if(s.getScore() > 0) {
                player.connection.send(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective.getName(), name, s.getScore()));
            }
        }

        player.connection.send(new ClientboundSetDisplayObjectivePacket(1, objective));

    }

    public void removePlayer(ServerPlayer player) {

        player.connection.send(new ClientboundSetDisplayObjectivePacket(1, null));

        for(int i = 0 ; i < teams.length ; i++) {
            player.connection.send(new ClientboundSetPlayerTeamPacket(teams[i], 1));

            String name = "§" + Integer.toHexString(i);
            player.connection.send(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective.getName(), name, 0));

        }

        player.connection.send(new ClientboundSetObjectivePacket(objective, 1));


    }

    public void clearPlayers() {
        for(int i = 0 ; i < players.size() ; i++) {
            removePlayer(players.get(0));
        }
    }

    public void update() {

        List<Packet<?>> packets = new ArrayList<>();

        if(objective.getDisplayName() != name) {
            packets.add(new ClientboundSetObjectivePacket(objective, 2));
            name = objective.getDisplayName();
        }

        for(int i = 0 ; i < teams.length ; i++) {
            if(updated[i]) {

                packets.add(new ClientboundSetPlayerTeamPacket(teams[i], 2));

                if(teams[i].getPlayerPrefix() == null) {
                    packets.add(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective.getName(), "§" + Integer.toHexString(i), 0));
                } else {
                    packets.add(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective.getName(), "§" + Integer.toHexString(i), i));
                }
                updated[i] = false;
            }
        }

        for(ServerPlayer player : players) {
            for(Packet<?> pck : packets) {
                player.connection.send(pck);
            }
        }

    }

}