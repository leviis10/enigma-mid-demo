package enigma.midtrans.service.implementation;

import enigma.midtrans.exceptions.UserNotFoundException;
import enigma.midtrans.model.User;
import enigma.midtrans.repository.UserRepository;
import enigma.midtrans.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public User findById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public void updateBalance(Integer id, Integer amount) {
        User foundUser = findById(id);
        foundUser.setBalance(foundUser.getBalance() + amount);
        userRepository.save(foundUser);
    }
}
