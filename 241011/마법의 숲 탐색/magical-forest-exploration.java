import java.io.*;
import java.util.*;

public class Main {
    static int R, C, K;
    static int[][] state;
    static Map<Integer, int[]> exit;

    static int answer;

    static int[][] dir = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}}; // 북 동 남 서

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        st = new StringTokenizer(br.readLine());
        R = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken());
        state = new int[R + 1][C + 1]; // 1 ~ R, 1 ~ C
        exit = new HashMap<>();

        for(int k = 1; k < K + 1; k++) {
            st = new StringTokenizer(br.readLine());
            int c = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());

            int r = -1; // (r, c) : 초기 정령 위치
            while(true) {
                if(goDown(r, c)) { // 남
                    r++;
                } else if(goLeft(r, c)) { // 서
                    r++;
                    c--;
                    d = d - 1 < 0 ? dir.length - 1 : d - 1;
                } else if(goRight(r, c)) { // 동
                    r++;
                    c++;
                    d = d + 1 >= dir.length ? 0 : d + 1;
                } else { // 움직일 수 없음
                    // 골렘의 몸 일부가 여전히 숲을 벗어난 상태인가?
                    boolean flag = true;
                    for(int direction = 0; direction < dir.length; direction++) {
                        int nr = r + dir[direction][0], nc = c + dir[direction][1];

                        if(nr < 1 || nr > R || nc < 1 || nc > C) { // 범위 밖
                            flag = false;
                            break;
                        }

                        state[nr][nc] = k;
                        if(direction == d) { // 출구 좌표
                            exit.put(k, new int[]{nr, nc});
                        }
                    }
                    state[r][c] = k;

                    if(!flag) { // 리셋
                        state = new int[R + 1][C + 1];
                        exit = new HashMap<>();
                        break;
                    }

                    // 정령이 가장 남쪽으로 이동
                    answer += calc(r, c);

                    break;
                }
            }
        }

        System.out.println(answer);
    }

    private static int calc(int r, int c) {
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[R + 1][C + 1];

        queue.offer(new int[] {r, c, state[r][c]});
        visited[r][c] = true;

        int row = 0;
        while(!queue.isEmpty()) {
            int[] now = queue.poll();
            r = now[0];
            c = now[1];
            int k = now[2];
            row = Math.max(row, r);

            for(int d = 0; d < dir.length; d++) {
                int nr = r + dir[d][0], nc = c + dir[d][1];

                if(nr < 1 || nr > R || nc < 1 || nc > C) {
                    continue;
                }

                if(visited[nr][nc]) {
                    continue;
                }
                
                if(state[nr][nc] == k) { // 같은 골렘
                    queue.offer(new int[] {nr, nc, k});
                    visited[nr][nc] = true;
                } else if(state[nr][nc] > 0) { // 다른 골렘
                    // 현재 위치가 출구라면 다른 골렘으로 넘어갈 수 있음
                    int[] info = exit.get(k);
                    if(info[0] != r || info[1] != c) {
                        continue;
                    }

                    queue.offer(new int[] {nr, nc, state[nr][nc]});
                    visited[nr][nc] = true;
                }
            }
        }

        return row;
    }

    private static boolean goRight(int r, int c) {
        if(c + 2 > C) {
            return false;
        }

        if(r - 1 > 0) {
            if(state[r - 1][c + 1] != 0) {
                return false;
            }
        }

        if(r > 0) {
            if(state[r][c + 2] != 0) {
                return false;
            }
        }

        if(r + 1 > 0) {
            if(state[r + 1][c + 1] != 0) {
                return false;
            }
        }

        return goDown(r, c + 1);
    }

    private static boolean goLeft(int r, int c) {
        if(c - 2 < 1) {
            return false;
        }

        if(r - 1 > 0 && c - 1 > 0) {
            if(state[r - 1][c - 1] != 0) {
                return false;
            }
        }

        if(r > 0) {
            if(state[r][c - 2] != 0) {
                return false;
            }
        }

        if(r + 1 > 0) {
            if(state[r + 1][c - 1] != 0) {
                return false;
            }
        }

        return goDown(r, c - 1);
    }

    private static boolean goDown(int r, int c) {
        if(r + 2 > R) {
            return false;
        }

        return state[r + 1][c - 1] == 0 && state[r + 2][c] == 0 && state[r + 1][c + 1] == 0;
    }
}