import 'package:flutter_test/flutter_test.dart';
import 'package:meme_arena/features/models.dart';

void main() {
  test('parses null accuracy', () {
    final s = ScoutStats.fromJson({'points': 0, 'rank': 'OBSERVER', 'accuracy': null});
    expect(s.accuracy, isNull);
  });

  test('parses prediction page', () {
    final p = PredictionPage.fromJson({'items': [{'id': 'p1', 'status': 'SUCCESS', 'meme': {'id': 'm1', 'title': 'Meme', 'contentUrl': '/c'}, 'battlesBeforeVote': 3, 'predictedAt': '2026-01-01T00:00:00Z', 'resolvedAt': '2026-01-02T00:00:00Z', 'pointsAwarded': 70}], 'page': 0, 'size': 20, 'totalElements': 1, 'totalPages': 1});
    expect(p.items.single.pointsAwarded, 70);
  });

  test('parses scout leaderboard and rank labels', () {
    final b = ScoutLeaderboard.fromJson({'period': 'WEEK', 'items': [{'position': 1, 'userId': 'u1', 'nickname': 'bob', 'scoutPoints': 10, 'rank': 'SCOUT', 'accuracy': 0.5, 'successfulPredictions': 1, 'bestSuccessStreak': 1}]});
    expect(b.items.single.nickname, 'bob');
    expect(rankLabel('TREND_HUNTER'), 'Охотник за трендами');
    expect(rankLabel('X'), 'X');
  });
}
