package enigma.midtrans.service;

import enigma.midtrans.model.User;

public interface UserService {
    User findById(Integer id);

    void updateBalance(Integer id, Integer amount);
}
