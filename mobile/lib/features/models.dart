class UserProfile {
  UserProfile({
    required this.id,
    required this.nickname,
    required this.votesCount,
    required this.submittedMemesCount,
    this.status,
    this.scout,
    this.achievements = const [],
  });

  final String id;
  final String nickname;
  final int votesCount;
  final int submittedMemesCount;
  final String? status;
  final ScoutStats? scout;
  final List<Achievement> achievements;

  factory UserProfile.fromJson(Map<String, dynamic> j) => UserProfile(
    id: j['id'].toString(),
    nickname: j['nickname'].toString(),
    votesCount: ((j['votesCount'] ?? 0) as num).toInt(),
    submittedMemesCount: ((j['submittedMemesCount'] ?? 0) as num).toInt(),
    status: j['status']?.toString(),
    scout: j['scout'] is Map ? ScoutStats.fromJson((j['scout'] as Map).cast()) : null,
    achievements: (j['achievements'] is List ? j['achievements'] as List : const [])
        .whereType<Map>()
        .map((e) => Achievement.fromJson(e.cast()))
        .toList(),
  );
}

class ScoutStats {
  ScoutStats({
    required this.points,
    required this.rank,
    required this.predictionsCount,
    required this.successfulPredictions,
    required this.failedPredictions,
    required this.expiredPredictions,
    required this.currentSuccessStreak,
    required this.bestSuccessStreak,
    this.accuracy,
  });

  final int points;
  final String rank;
  final int predictionsCount;
  final int successfulPredictions;
  final int failedPredictions;
  final int expiredPredictions;
  final double? accuracy;
  final int currentSuccessStreak;
  final int bestSuccessStreak;

  factory ScoutStats.fromJson(Map<String, dynamic> j) => ScoutStats(
    points: ((j['points'] ?? 0) as num).toInt(),
    rank: (j['rank'] ?? '').toString(),
    predictionsCount: ((j['predictionsCount'] ?? 0) as num).toInt(),
    successfulPredictions: ((j['successfulPredictions'] ?? 0) as num).toInt(),
    failedPredictions: ((j['failedPredictions'] ?? 0) as num).toInt(),
    expiredPredictions: ((j['expiredPredictions'] ?? 0) as num).toInt(),
    accuracy: j['accuracy'] == null ? null : (j['accuracy'] as num).toDouble(),
    currentSuccessStreak: ((j['currentSuccessStreak'] ?? 0) as num).toInt(),
    bestSuccessStreak: ((j['bestSuccessStreak'] ?? 0) as num).toInt(),
  );
}

class Achievement {
  Achievement({required this.code, required this.title, required this.description, this.unlockedAt});

  final String code;
  final String title;
  final String description;
  final String? unlockedAt;

  factory Achievement.fromJson(Map<String, dynamic> j) => Achievement(
    code: (j['code'] ?? '').toString(),
    title: (j['title'] ?? '').toString(),
    description: (j['description'] ?? '').toString(),
    unlockedAt: j['unlockedAt']?.toString(),
  );
}

class MemeItem{MemeItem({required this.id,required this.title,required this.imageUrl,required this.rating,this.wins=0,this.losses=0,this.battlesCount=0,this.position});final String id,title,imageUrl;final int rating,wins,losses,battlesCount;final int? position;factory MemeItem.fromJson(Map<String,dynamic> j)=>MemeItem(id:(j['id']??j['memeId']).toString(),title:j['title'].toString(),imageUrl:(j['contentUrl']??j['imageUrl']??'').toString(),rating:((j['rating']??0)as num).toInt(),wins:((j['wins']??0)as num).toInt(),losses:((j['losses']??0)as num).toInt(),battlesCount:((j['battlesCount']??j['periodBattles']??0)as num).toInt(),position:j['position']==null?null:(j['position']as num).toInt());}
class Battle{Battle({required this.battleId,required this.left,required this.right});final String battleId;final MemeItem left,right;factory Battle.fromJson(Map<String,dynamic> j)=>Battle(battleId:j['battleId'].toString(),left:MemeItem.fromJson((j['left']as Map).cast()),right:MemeItem.fromJson((j['right']as Map).cast()));}
class MediaUpload{MediaUpload(this.id,this.contentUrl);final String id,contentUrl;factory MediaUpload.fromJson(Map<String,dynamic> j)=>MediaUpload(j['id'].toString(),(j['contentUrl']??'').toString());}
String? validateNickname(String v){final t=v.trim();if(t.length<3||t.length>30)return 'Ник должен быть от 3 до 30 символов';if(!RegExp(r'^[A-Za-zА-Яа-яЁё0-9_ -]+$').hasMatch(t))return 'Используйте буквы, цифры, пробел, _ или -';return null;}
String periodCode(int index)=>['DAY','WEEK','ALL_TIME'][index];
