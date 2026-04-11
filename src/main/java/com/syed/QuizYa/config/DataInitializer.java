package com.syed.QuizYa.config;

import com.syed.QuizYa.model.QuestionBank;
import com.syed.QuizYa.model.Question;
import com.syed.QuizYa.model.QuestionOption;
import com.syed.QuizYa.repository.QuestionBankRepository;
import com.syed.QuizYa.repository.QuestionOptionRepository;
import com.syed.QuizYa.repository.QuestionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    // Standard quiz colors: Red, Blue, Yellow, Green
    private static final String[] COLORS = {"#E21B3C", "#1368CE", "#D89E00", "#26890C"};

    @Bean
    public CommandLineRunner initCuratedBanks(
            QuestionBankRepository bankRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository optionRepository) {

        return args -> {
            List<QuestionBank> curatedBanks = bankRepository.findByIsCuratedTrue();
            if (curatedBanks.isEmpty()) {
                createSocialMixerBank(bankRepository, questionRepository, optionRepository);
                createWeddingBank(bankRepository, questionRepository, optionRepository);
                createFamilyReunionBank(bankRepository, questionRepository, optionRepository);
            }
        };
    }

    private void createSocialMixerBank(QuestionBankRepository bankRepo, QuestionRepository qRepo, QuestionOptionRepository optRepo) {
        QuestionBank bank = new QuestionBank();
        bank.setTitle("Social Mixer & Party Trivia");
        bank.setDescription("Break the ice and get everyone talking at your next gathering.");
        bank.setCurated(true);
        bankRepo.save(bank);

        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "What is the ultimate party snack? 🍕", 1, 30,
                new String[]{"Pizza", "Chips & Dip", "Chicken Wings", "Veggie Platter"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "If you could have a superpower, what would it be? 🦸", 2, 30,
                new String[]{"Flight", "Invisibility", "Telepathy", "Time Travel"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "What is your preferred weekend activity? 🏕️", 3, 30,
                new String[]{"Hiking/Outdoors", "Binge-watching TV", "Gaming", "Going to a Party"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Are you an early bird or a night owl? 🦉", 4, 20,
                new String[]{"Early Bird", "Night Owl", "A bit of both", "Permanently Exhausted"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "MULTIPLE_CHOICE", "Trivia: Which of these is technically a berry? 🍓", 5, 20,
                new String[]{"Strawberry", "Raspberry", "Banana", "Blackberry"}, 2);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "What is your go-to music genre? 🎵", 6, 30,
                new String[]{"Pop/Chart Hits", "Rock/Alternative", "Hip-hop/R&B", "Electronic/Dance"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "What's the best conversation starter? 💬", 7, 30,
                new String[]{"The Weather", "Current Movies/TV", "Travel Stories", "Food/Restaurants"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Cats or Dogs? 🐶🐱", 8, 20,
                new String[]{"Dogs absolutely", "Cats definitely", "Love both equally", "Not a pet person"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "What is your dream vacation destination? ✈️", 9, 30,
                new String[]{"Tropical Beach", "Historic City", "Snowy Mountains", "Quiet Countryside"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "MULTIPLE_CHOICE", "Trivia: How many time zones are there in the world? 🌍", 10, 20,
                new String[]{"12", "24", "36", "48"}, 1);
    }

    private void createWeddingBank(QuestionBankRepository bankRepo, QuestionRepository qRepo, QuestionOptionRepository optRepo) {
        QuestionBank bank = new QuestionBank();
        bank.setTitle("Wedding Celebration");
        bank.setDescription("Fun questions about the happy couple and wedding traditions.");
        bank.setCurated(true);
        bankRepo.save(bank);

        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who takes longer to get ready? ⏱️", 1, 30,
                new String[]{"The Bride", "The Groom", "It's a tie", "Neither, they are fast"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who said 'I love you' first? ❤️", 2, 20,
                new String[]{"The Bride", "The Groom", "At the same time", "No one remembers"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "What is the best part of a wedding? 🥂", 3, 30,
                new String[]{"The Food", "The Dancing", "The Vows", "Open Bar"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who is the better cook? 🍳", 4, 20,
                new String[]{"The Bride", "The Groom", "They share cooking", "Takeout is their chef"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Where was their first date? ☕", 5, 20,
                new String[]{"Restaurant", "Coffee Shop", "Movie Theater", "A Park"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who is more organized? 📅", 6, 20,
                new String[]{"The Bride", "The Groom", "Both are highly organized", "Beautiful chaos"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "What's their favorite thing to do together? 🍿", 7, 30,
                new String[]{"Travel the world", "Binge-watch shows", "Try new restaurants", "Outdoor adventures"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who is the better driver? 🚗", 8, 20,
                new String[]{"The Bride", "The Groom", "Both are great", "Let's not talk about it"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who is most likely to steal the covers at night? 🛏️", 9, 20,
                new String[]{"The Bride", "The Groom", "The Pet", "They share perfectly"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "What advice would you give the newlyweds? 💌", 10, 30,
                new String[]{"Never go to bed angry", "Always say 'Yes Dear'", "Laugh every day", "Travel often"}, -1);
    }

    private void createFamilyReunionBank(QuestionBankRepository bankRepo, QuestionRepository qRepo, QuestionOptionRepository optRepo) {
        QuestionBank bank = new QuestionBank();
        bank.setTitle("Family Reunion Fun");
        bank.setDescription("Nostalgia, inside jokes, and friendly family competition.");
        bank.setCurated(true);
        bankRepo.save(bank);

        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who is the ultimate family prankster? 🃏", 1, 30,
                new String[]{"An Uncle/Aunt", "A Sibling/Cousin", "A Grandparent", "A Parent"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "What is the best family tradition? 🎄", 2, 30,
                new String[]{"Holiday Gatherings", "Annual Vacations", "Sunday Dinners", "Game Nights"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who makes the best food in the family? 🍲", 3, 20,
                new String[]{"Grandma/Grandpa", "Mom", "Dad", "Someone else"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who is most likely to fall asleep on the couch today? 😴", 4, 20,
                new String[]{"Dad", "Grandpa", "A Baby/Toddler", "A Teenager"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "What is the best type of family vacation? 🏖️", 5, 30,
                new String[]{"Beach House", "Cabin in the Woods", "Theme Park", "Cross-Country Road Trip"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who is the family photographer? 📸", 6, 20,
                new String[]{"Mom", "Aunt", "The Gen Z Teenager", "Grandma"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who is the most competitive during board games? 🎲", 7, 30,
                new String[]{"A Sibling", "Dad", "A Cousin", "An Uncle"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who tells the best stories? 📖", 8, 20,
                new String[]{"Grandpa", "An Uncle", "Mom", "An Aunt"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Which holiday is the most chaotic for our family? 🦃", 9, 30,
                new String[]{"Thanksgiving", "Christmas/Winter Holidays", "Fourth of July/Summer BBQ", "Birthdays"}, -1);
        addQuestionWithOptions(qRepo, optRepo, bank, "POLL", "Who is notoriously always late? ⏰", 10, 20,
                new String[]{"A Sibling", "A Teenager", "An Uncle", "An Aunt"}, -1);
    }

    /**
     * Helper method to create a question and its associated options cleanly.
     * @param correctIndex The index (0-3) of the correct option. Pass -1 for POLLS where there is no correct answer.
     */
    private void addQuestionWithOptions(QuestionRepository qRepo, QuestionOptionRepository oRepo,
                                        QuestionBank bank, String type, String prompt, int order,
                                        int timeLimit, String[] options, int correctIndex) {
        
        Question q = new Question();
        q.setBank(bank);
        q.setQuestionType(type);
        q.setPromptText(prompt);
        q.setSortOrder(order);
        q.setTimeLimitSeconds(timeLimit);
        qRepo.save(q);

        for (int i = 0; i < options.length; i++) {
            QuestionOption opt = new QuestionOption();
            opt.setQuestion(q);
            opt.setOptionText(options[i]);
            opt.setCorrect(i == correctIndex);
            opt.setSortOrder(i + 1);
            opt.setColorHex(COLORS[i % COLORS.length]);
            oRepo.save(opt);
        }
    }
}