package eu.lpinto.sun.controllers;

import eu.lpinto.sun.persistence.entities.Token;
import eu.lpinto.sun.persistence.entities.User;
import eu.lpinto.sun.persistence.facades.TokenFacade;
import eu.lpinto.sun.persistence.facades.UserFacade;
import eu.lpinto.universe.api.util.Digest;
import eu.lpinto.universe.controllers.exceptions.PreConditionException;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Controller for Token entity.
 *
 * @author Luis Pinto <code>- mail@lpinto.eu</code>
 */
@Stateless
public class TokenController {

    @EJB
    private TokenFacade facade;

    @EJB
    private UserFacade userFacade;

    static final String SALT = "eu.lpinto.star.TokenController";

    /*
     * Custom controller services
     */
    public Token login(final User user) throws PreConditionException {
        String accessToken = Digest.getSHA(user.getId() + "." + String.valueOf(System.currentTimeMillis()), SALT);

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new PreConditionException("token.email", "Missing email");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new PreConditionException("token.email", "Missing password");
        }

        User savedUser = userFacade.findByEmail(user.getEmail());

        if (savedUser == null) {
            throw new PreConditionException("username", "unknownUser");
        }

        if (!savedUser.getPassword().equals(Digest.getSHA(user.getPassword()))) {
            throw new PreConditionException("password", "invalidPassword");
        }

        Token newToken = new Token(accessToken, savedUser);
        facade.create(newToken);

        return newToken;
    }

    public void logout(final Long userID, final String token) {
        Token session;

        session = facade.findByToken(token);

        facade.remove(session);
    }

    public User validate(final String token) {
        Token session;

        session = facade.findByToken(token);

        if (session == null) {
            return null;
        }

        return session.getUser();
    }

    public TokenFacade getFacade() {
        return facade;
    }
}
