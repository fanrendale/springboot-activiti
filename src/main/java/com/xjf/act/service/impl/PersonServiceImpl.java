package com.xjf.act.service.impl;

import com.xjf.act.entity.Person;
import com.xjf.act.mapper.PersonMapper;
import com.xjf.act.service.PersonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xjf
 * @since 2019-12-21
 */
@Service
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements PersonService {

}
