import java.io.*;
import java.util.*;

class Santa {
	int x, y; // 좌표
	int score; // 점수
	boolean isOut; // 탈락 여부
	int isStun; // 기절 여부 (0 : 기절 아님, turn - isStun >= 2 : 깨어남)
	
	public Santa(int x, int y, int score, boolean isOut, int isStun) {
		this.x = x;
		this.y = y;
		this.score = score;
		this.isOut = isOut;
		this.isStun = isStun;
	}
}

public class Main {
	static int N, M, P, C, D;
	static int RX, RY; // 루돌프의 초기 위치
	static Santa[] santa; // 산타 정보
	static Map<Integer, Integer> isSanta; // 해당 좌표에 산타가 있는가?
	
	static int[][] dir = {
			{0, -1}, {1, 0}, {0, 1}, {-1, 0}, // 좌 하 우 상
			{1, 1}, {-1, -1}, {1, -1}, {-1, 1}
	};
	
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringBuilder sb = new StringBuilder();
		StringTokenizer st;
		
		st = new StringTokenizer(br.readLine(), " ");
		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		P = Integer.parseInt(st.nextToken());
		C = Integer.parseInt(st.nextToken());
		D = Integer.parseInt(st.nextToken());
		
		st = new StringTokenizer(br.readLine(), " ");
		RX = Integer.parseInt(st.nextToken()) - 1;
		RY = Integer.parseInt(st.nextToken()) - 1;
		
		santa = new Santa[P + 1];
		isSanta = new HashMap<>();
		for(int i = 0; i < P; i++) {
			st = new StringTokenizer(br.readLine(), " ");
			int p = Integer.parseInt(st.nextToken());
			int sx = Integer.parseInt(st.nextToken()) - 1;
			int sy = Integer.parseInt(st.nextToken()) - 1;
			
			santa[p] = new Santa(sx, sy, 0, false, 0);
			isSanta.put(sx * N + sy, p);
		}
		
		for(int turn = 1; turn < M + 1; turn++) {
			// [ 루돌프의 움직임 ]
			PriorityQueue<int[]> pq = new PriorityQueue<>((o1, o2) -> {
				if(Integer.compare(o1[0], o2[0]) == 0) {
					if(Integer.compare(o2[1], o1[1]) == 0) {
						return Integer.compare(o2[2], o1[2]); // c 좌표가 큰 순
					}
					return Integer.compare(o2[1], o1[1]); // r 좌표가 큰 순
				}
				return Integer.compare(o1[0], o2[0]); // 거리 가까운 순
			});
			
			// 가장 가까운 산타 찾기
			for(int p = 1; p < P + 1; p++) {
				Santa s = santa[p];
				if(s.isOut) { // 탈락한 산타
					continue;
				}
				
				int newDist = (s.x - RX) * (s.x - RX) + (s.y - RY) * (s.y - RY);
				pq.offer(new int[] {newDist, s.x, s.y});
			}
			int[] target = pq.poll();
			int dist = target[0], sx = target[1], sy = target[2];
			
			// 루돌프 돌진 -> 8방 중 산타에게 가장 가까운 방향으로 이동
			int nd = 0; // 해당 방향으로 이동
			for(int d = 0; d < dir.length; d++) {
				int nx = RX + dir[d][0], ny = RY + dir[d][1];
				
				if(!checkArea(nx, ny)) { // 범위 밖
					continue;
				}
				
				int newDist = (nx - sx) * (nx - sx) + (ny - sy) * (ny - sy);
				if(dist > newDist) {
					nd = d;
					dist = newDist;
				}
			}
			RX += dir[nd][0];
			RY += dir[nd][1];
			
			// 루돌프와 산타 충돌
			if(isSanta.containsKey(RX * N + RY)) {
				int p = isSanta.get(RX * N + RY); // 이동한 위치에 있는 산타
				isSanta.remove(RX * N + RY); // 산타 삭제
				
				santa[p].score += C; // 점수 획득
				santa[p].isStun = turn; // 기절
				
				int nx = santa[p].x + dir[nd][0] * C, ny = santa[p].y + dir[nd][1] * C; // 다음 위치
				if(!checkArea(nx, ny)) { // 범위 밖 -> 탈락
					santa[p].isOut = true;
				} else if(!isSanta.containsKey(nx * N + ny)) { // 산타 없음
					isSanta.put(nx * N + ny, p);
					santa[p].x = nx;
					santa[p].y = ny;
				} else { // 상호작용 발생
					interaction(p, nx, ny, nd); // p 산타를 nd 방향으로 이동
				}
			}
			
			// [ 산타의 움직임 ]
			for(int p = 1; p < P + 1; p++) {
				Santa s = santa[p];
				if(s.isOut) { // 탈락한 산타
					continue;
				}
				if(s.isStun != 0) { // 기절한 산타
					if(turn - s.isStun >= 2) { // 깨우고 시작
						s.isStun = 0;
					} else { // 안 움직임
						continue;
					}
				}
				
				// 산타 이동
				dist = (RX - s.x) * (RX - s.x) + (RY - s.y) * (RY - s.y); // 산타 <-> 루돌프
				nd = 0; // 해당 방향으로 이동
				
				int ndist = Integer.MAX_VALUE; // 4방에서 가장 가까운 거리 찾기
				for(int d = 0; d < 4; d++) {
					int nx = s.x + dir[d][0], ny = s.y + dir[d][1];
					
					if(!checkArea(nx, ny)) { // 범위 밖
						continue;
					}
					if(isSanta.containsKey(nx * N + ny)) { // 이미 해당 위치에 다른 산타가 있음
						continue;
					}
				
					int newDist = (RX - nx) * (RX - nx) + (RY - ny) * (RY - ny);
					if(ndist < newDist) { // 최단 거리 갱신 불가능
						continue;
					}
					
					nd = d;
					ndist = newDist;
				}
				
				if(ndist < dist) {
					isSanta.remove(santa[p].x * N + santa[p].y); // 현재 위치에서 산타 삭제
					santa[p].x += dir[nd][0];
					santa[p].y += dir[nd][1];
					
					if(santa[p].x == RX && santa[p].y == RY) { // 산타와 루돌프 충돌
						santa[p].score += D; // 점수 획득
						santa[p].isStun = turn; // 기절
						
						if(nd == 0 || nd == 1) { // 반대 방향으로 
							nd += 2;
						} else {
							nd -= 2;
						}
						int nx = santa[p].x + dir[nd][0] * D, ny = santa[p].y + dir[nd][1] * D; // 다음 위치
						
						if(!checkArea(nx, ny)) { // 범위 밖 -> 탈락
							santa[p].isOut = true;
						} else if(!isSanta.containsKey(nx * N + ny)) { // 산타 없음
							isSanta.put(nx * N + ny, p);
							santa[p].x = nx;
							santa[p].y = ny;
						} else { // 상호작용 발생
							interaction(p, nx, ny, nd);
						}
					} else {
						isSanta.put(santa[p].x * N + santa[p].y, p);
					}
				}
			}
			
			int cnt = 0; // 생존한 산타의 수
			for(int p = 1; p < P + 1; p++) {
				if(santa[p].isOut) {
					continue;
				}
				
				cnt++;
				santa[p].score++; // 생존한 산타 점수 올리기
			}
			if(cnt == 0) { // 모든 산타가 탈락한 경우 -> 종료
				break;
			}
		}
		
		for(int p = 1; p < P + 1; p++) {
			sb.append(santa[p].score).append(" ");
		}
		System.out.println(sb.toString());
	}

	private static void interaction(int p, int nx, int ny, int d) { // 현재 위치에 있는 산타, 이동 방향
		while(true) {
			if(!checkArea(nx, ny)) { // 범위 밖
				santa[p].isOut = true;
				break;
			}
			
			if(!isSanta.containsKey(nx * N + ny)) { // 산타 없음
				isSanta.put(nx * N + ny, p);
				santa[p].x = nx;
				santa[p].y = ny;
				break;
			}
			
			int temp = isSanta.get(nx * N + ny); // 다음 위치에 있는 산타
			isSanta.put(nx * N + ny, p); // 다음 위치에 현재 산타 세팅
			santa[p].x = nx;
			santa[p].y = ny;
			
			p = temp;
			nx = santa[p].x + dir[d][0];
			ny = santa[p].y + dir[d][1];
		}
	}

	private static boolean checkArea(int x, int y) {
		return !(x < 0 || x >= N || y < 0 || y >= N);
	}
}