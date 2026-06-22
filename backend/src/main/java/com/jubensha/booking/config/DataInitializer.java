package com.jubensha.booking.config;

import com.jubensha.booking.entity.Room;
import com.jubensha.booking.entity.Script;
import com.jubensha.booking.repository.RoomRepository;
import com.jubensha.booking.repository.ScriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ScriptRepository scriptRepository;
    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) {
        if (scriptRepository.count() == 0) {
            initScripts();
        }
        if (roomRepository.count() == 0) {
            initRooms();
        }
    }

    private void initScripts() {
        Script s1 = new Script();
        s1.setName("古木吟");
        s1.setTotalPlayers(6);
        s1.setMinMale(3);
        s1.setMinFemale(3);
        s1.setPrice(128);
        s1.setDuration(240);
        s1.setDescription("情感沉浸本，校园背景，哭哭本首选");
        scriptRepository.save(s1);

        Script s2 = new Script();
        s2.setName("病娇男孩的精分日记");
        s2.setTotalPlayers(7);
        s2.setMinMale(4);
        s2.setMinFemale(2);
        s2.setPrice(138);
        s2.setDuration(300);
        s2.setDescription("硬核推理本，多重人格，烧脑神作");
        scriptRepository.save(s2);

        Script s3 = new Script();
        s3.setName("漓川怪谈簿");
        s3.setTotalPlayers(7);
        s3.setMinMale(3);
        s3.setMinFemale(4);
        s3.setPrice(148);
        s3.setDuration(360);
        s3.setDescription("日式妖怪本，变格推理，机制新颖");
        scriptRepository.save(s3);

        Script s4 = new Script();
        s4.setName("年轮");
        s4.setTotalPlayers(5);
        s4.setMinMale(3);
        s4.setMinFemale(2);
        s4.setPrice(118);
        s4.setDuration(240);
        s4.setDescription("经典硬核本，逻辑闭环，口碑神作");
        scriptRepository.save(s4);

        Script s5 = new Script();
        s5.setName("来电");
        s5.setTotalPlayers(6);
        s5.setMinMale(3);
        s5.setMinFemale(3);
        s5.setPrice(98);
        s5.setDuration(180);
        s5.setDescription("欢乐机制本，新手友好，团建推荐");
        scriptRepository.save(s5);

        Script s6 = new Script();
        s6.setName("窗边的女人");
        s6.setTotalPlayers(6);
        s6.setMinMale(4);
        s6.setMinFemale(2);
        s6.setPrice(108);
        s6.setDuration(240);
        s6.setDescription("本格推理本，真实案件改编，细思极恐");
        scriptRepository.save(s6);
    }

    private void initRooms() {
        Room r1 = new Room();
        r1.setName("一号厅·日式");
        r1.setCapacity(8);
        r1.setDescription("日式榻榻米风格，适合沉浸本");
        roomRepository.save(r1);

        Room r2 = new Room();
        r2.setName("二号厅·古风");
        r2.setCapacity(10);
        r2.setDescription("中式古风装修，适合古装本");
        roomRepository.save(r2);

        Room r3 = new Room();
        r3.setName("三号厅·现代");
        r3.setCapacity(7);
        r3.setDescription("现代简约风格，适合硬核本");
        roomRepository.save(r3);

        Room r4 = new Room();
        r4.setName("四号厅·恐怖");
        r4.setCapacity(6);
        r4.setDescription("暗黑恐怖风格，适合恐怖本");
        roomRepository.save(r4);

        Room r5 = new Room();
        r5.setName("五号厅·欢乐");
        r5.setCapacity(9);
        r5.setDescription("明亮活泼风格，适合欢乐机制本");
        roomRepository.save(r5);
    }
}
