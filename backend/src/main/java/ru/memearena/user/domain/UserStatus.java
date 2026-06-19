package ru.memearena.user.domain;
public enum UserStatus { NEWBIE, SCOUT, MEME_LORD; public static UserStatus fromVotes(long votes){ if(votes>=100)return MEME_LORD; if(votes>=10)return SCOUT; return NEWBIE; } }
