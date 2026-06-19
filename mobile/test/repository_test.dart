import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:meme_arena/core/network/session_store.dart';
import 'package:meme_arena/features/api_repository.dart';

class MockAdapter extends HttpClientAdapter {
  MockAdapter(this.handler);
  final ResponseBody Function(RequestOptions) handler;

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<List<int>>? requestStream,
    Future<void>? cancelFuture,
  ) async => handler(options);

  @override
  void close({bool force = false}) {}
}

class FakeSessionStore extends SessionStore {
  FakeSessionStore() : super(const FlutterSecureStorage());

  String? savedUserId;
  String? savedNickname;
  String? savedAccessToken;

  @override
  Future<String> installationId() async => 'install-1';

  @override
  Future<void> saveUser({required String userId, required String nickname, required String accessToken}) async {
    savedUserId = userId;
    savedNickname = nickname;
    savedAccessToken = accessToken;
  }

  @override
  Future<void> clearUserSession() async {
    savedUserId = null;
    savedNickname = null;
    savedAccessToken = null;
  }
}

ResponseBody jsonBody(Object o, {int status = 200}) => ResponseBody.fromString(
  jsonEncode(o),
  status,
  headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
);

void main() {
  late Dio dio;
  late FakeSessionStore store;
  late ApiRepository repo;

  setUp(() {
    dio = Dio(BaseOptions(baseUrl: 'http://x'));
    store = FakeSessionStore();
    repo = ApiRepository(dio, store);
  });

  test('create guest user stores auth session', () async {
    dio.httpClientAdapter = MockAdapter((o) {
      expect(o.path, '/api/v1/users/guest');
      expect((o.data as Map)['installationId'], 'install-1');
      return jsonBody({
        'user': {'id': 'u1', 'nickname': 'bob', 'votesCount': 0, 'submittedMemesCount': 0},
        'accessToken': 'token-1',
        'expiresAt': '2026-01-01T00:00:00Z',
      }, status: 201);
    });

    expect((await repo.createGuest('bob')).nickname, 'bob');
    expect(store.savedUserId, 'u1');
    expect(store.savedAccessToken, 'token-1');
  });

  test('get profile parses scout stats and achievements', () async {
    dio.httpClientAdapter = MockAdapter((o) => jsonBody({
      'id': 'u1',
      'nickname': 'bob',
      'votesCount': 2,
      'submittedMemesCount': 1,
      'scout': {'points': 10, 'rank': 'SCOUT', 'predictionsCount': 1, 'successfulPredictions': 1, 'failedPredictions': 0, 'expiredPredictions': 0, 'accuracy': 1.0, 'currentSuccessStreak': 1, 'bestSuccessStreak': 1},
      'achievements': [{'code': 'FIRST_HIT', 'title': 'Hit', 'description': 'Desc', 'unlockedAt': '2026-01-01T00:00:00Z'}],
    }));

    final profile = await repo.getProfile('u1');
    expect(profile.votesCount, 2);
    expect(profile.scout!.points, 10);
    expect(profile.achievements.single.code, 'FIRST_HIT');
  });

  test('get next battle', () async {
    dio.httpClientAdapter = MockAdapter((o) => jsonBody({'battleId': 'b', 'left': {'id': 'l', 'title': 'L', 'contentUrl': '/l', 'rating': 1}, 'right': {'id': 'r', 'title': 'R', 'contentUrl': '/r', 'rating': 2}}));
    expect((await repo.getNextBattle())!.left.title, 'L');
  });

  test('204 no battle', () async {
    dio.httpClientAdapter = MockAdapter((o) => ResponseBody.fromString('', 204));
    expect(await repo.getNextBattle(), isNull);
  });

  test('submit vote', () async {
    dio.httpClientAdapter = MockAdapter((o) {
      expect(o.path, '/api/v1/votes');
      return jsonBody({}, status: 201);
    });
    final b = await ApiRepository(
      Dio()..httpClientAdapter = MockAdapter((o) => jsonBody({'battleId': 'b', 'left': {'id': 'l', 'title': 'L', 'imageUrl': '/l', 'rating': 1}, 'right': {'id': 'r', 'title': 'R', 'imageUrl': '/r', 'rating': 2}})),
      store,
    ).getNextBattle();
    await repo.submitVote(b!, 'l');
  });

  test('get top', () async {
    dio.httpClientAdapter = MockAdapter((o) => jsonBody({'items': [{'position': 1, 'memeId': 'm', 'title': 'T', 'contentUrl': '/m', 'rating': 9, 'wins': 1, 'losses': 0, 'battlesCount': 1}]}));
    expect((await repo.getTop()).single.battlesCount, 1);
  });

  test('create meme with imageUrl', () async {
    dio.httpClientAdapter = MockAdapter((o) {
      expect((o.data as Map)['imageUrl'], 'http://i');
      return jsonBody({}, status: 201);
    });
    await repo.createMeme(title: 't', imageUrl: 'http://i');
  });

  test('upload image if endpoint implemented method exists', () {
    expect(repo.uploadImage, isA<Function>());
  });
}
