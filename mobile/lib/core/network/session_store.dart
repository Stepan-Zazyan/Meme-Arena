import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';

class SessionStore {
  SessionStore(this._secure);
  final FlutterSecureStorage _secure;
  static const _tokenKey = 'accessToken';
  static const _userIdKey = 'userId';
  static const _nicknameKey = 'nickname';
  static const _installationIdKey = 'installationId';

  Future<String?> accessToken() => _secure.read(key: _tokenKey);
  Future<void> setAccessToken(String token) => _secure.write(key: _tokenKey, value: token);
  Future<void> clearAccessToken() => _secure.delete(key: _tokenKey);
  Future<String?> userId() async => (await SharedPreferences.getInstance()).getString(_userIdKey);
  Future<String?> nickname() async => (await SharedPreferences.getInstance()).getString(_nicknameKey);
  Future<void> saveUser({required String userId, required String nickname, required String accessToken}) async { final p=await SharedPreferences.getInstance(); await p.setString(_userIdKey,userId); await p.setString(_nicknameKey,nickname); await setAccessToken(accessToken); }
  Future<String> installationId() async { final p=await SharedPreferences.getInstance(); var id=p.getString(_installationIdKey); if(id==null){ id=const Uuid().v4(); await p.setString(_installationIdKey,id); } return id; }
  Future<void> clearUserSession() async { final p=await SharedPreferences.getInstance(); await p.remove(_userIdKey); await p.remove(_nicknameKey); await clearAccessToken(); }
  Future<void> migrateLegacyIfNeeded() async { final id=await userId(); final token=await accessToken(); if(id!=null && token==null){ await clearUserSession(); await installationId(); } }
}
