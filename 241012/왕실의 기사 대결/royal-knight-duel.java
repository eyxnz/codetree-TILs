import java.io.*;
import java.util.*;

class Info {
    int r;
    int c;
    int h;
    int w;
    int k;
    int damage;

    public Info(int r, int c, int h, int w, int k) {
        this.r = r;
        this.c = c;
        this.h = h;
        this.w = w;
        this.k = k;
        this.damage = 0;
    }
}

public class Main {
    static int L, N, Q;
    static int[][] arr; // 체크판

    static Info[] infos; // 기사 정보
    static int[][] position; // 현재 위치에 몇 번 기사가 있는지
    static PriorityQueue<int[]> target; // 밀린 기사 정보

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        st = new StringTokenizer(br.readLine(), " ");
        L = Integer.parseInt(st.nextToken());
        N = Integer.parseInt(st.nextToken());
        Q = Integer.parseInt(st.nextToken());
        arr = new int[L][L];

        infos = new Info[N + 1];
        position = new int[L][L];

        for(int i = 0; i < L; i++) {
            st = new StringTokenizer(br.readLine(), " ");

            for(int j = 0; j < L; j++) {
                arr[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        for(int n = 1; n < N + 1; n++) {
            st = new StringTokenizer(br.readLine(), " ");
            int r = Integer.parseInt(st.nextToken()) - 1;
            int c = Integer.parseInt(st.nextToken()) - 1;
            int h = Integer.parseInt(st.nextToken());
            int w = Integer.parseInt(st.nextToken());
            int k = Integer.parseInt(st.nextToken());

            infos[n] = new Info(r, c, h, w, k);
            for(int i = 0; i < h; i++) {
                for(int j = 0; j < w; j++) {
                    position[r + i][c + j] = n;
                }
            }
        }

        for(int q = 0; q < Q; q++) {
            st = new StringTokenizer(br.readLine(), " ");
            int i = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());

            if(infos[i] == null) { // 이미 사라진 기사
                continue;
            }

            target = new PriorityQueue<>((o1, o2) -> {
                return o2[0] - o1[0]; // depth가 깊은 순
            });

            if(d == 0) { // 위쪽
                if(!up(i, 0)) {
                    continue;
                }
            } else if(d == 1) { // 오른쪽
                if(!right(i, 0)) {
                    continue;
                }
            } else if(d == 2) { // 아래쪽
                if(!down(i, 0)) {
                    continue;
                }
            } else if(d == 3) { // 왼쪽
                if(!left(i, 0)) {
                    continue;
                }
            }

            // 피해 입은 기사 탐색
            while(!target.isEmpty()) {
                int[] now = target.poll();
                int n = now[1];

                if(i == n) {
                    continue;
                }

                if(d == 0) { // 위쪽
                    moveUp(n);
                } else if(d == 1) { // 오른쪽
                    moveRight(n);
                } else if(d == 2) { // 아래쪽
                    moveDown(n);
                } else if(d == 3) { // 왼쪽
                    moveLeft(n);
                }

                int cnt = 0; // 함정의 개수
                for(int h = 0; h < infos[n].h; h++) {
                    for(int w = 0; w < infos[n].w; w++) {
                        int r = infos[n].r + h, c = infos[n].c + w;

                        if(arr[r][c] == 1) {
                            cnt++;
                        }
                    }
                }

                infos[n].k = infos[n].k - cnt;
                infos[n].damage = infos[n].damage + cnt;
                if(infos[n].k <= 0) {
                    for(int h = 0; h < infos[n].h; h++) {
                        for(int w = 0; w < infos[n].w; w++) {
                            int r = infos[n].r + h, c = infos[n].c + w;

                            position[r][c] = 0;
                        }
                    }
                    infos[n] = null;
                }
            }
        }

        int answer = 0;
        for(int n = 1; n < N + 1; n++) {
            if(infos[n] == null) {
                continue;
            }

            answer += infos[n].damage;
        }
        System.out.println(answer);
    }

    private static boolean up(int num, int depth) { // num번 기사를 위로 밀 수 있는가?
        // 맨 윗 칸인 경우
        if(infos[num].r == 0) {
            return false;
        }

        // 윗 칸 탐색
        Set<Integer> another = new HashSet<>(); // 다른 기사 존재 여부
        for(int w = 0; w < infos[num].w; w++) {
            int r = infos[num].r - 1, c = infos[num].c + w;

            if(arr[r][c] == 2) { // 벽 존재
                return false;
            }

            if(position[r][c] != 0) { // 다른 기사 존재
                another.add(position[r][c]);
            }
        }

        // 윗 칸에 있는 다른 기사들을 이동시키지 못한 경우
        for(Integer n : another) {
            if(!up(n, depth + 1)) {
                return false;
            }
        }

        target.offer(new int[] {depth, num});

        return true;
    }

    private static void moveUp(int num) {
        for(int w = 0; w < infos[num].w; w++) {
            position[infos[num].r + infos[num].h - 1][infos[num].c + w] = 0;
            position[infos[num].r - 1][infos[num].c + w] = num;
        }
        infos[num].r = infos[num].r - 1;
    }

    private static boolean right(int num, int depth) { // num번 기사를 오른쪽으로 밀 수 있는가?
        // 맨 오른쪽 칸인 경우
        if(infos[num].c + infos[num].w == L) {
            return false;
        }

        // 오른쪽 칸 탐색
        boolean block = false; // 벽 존재 여부
        Set<Integer> another = new HashSet<>(); // 다른 기사 존재 여부
        for(int h = 0; h < infos[num].h; h++) {
            int r = infos[num].r + h, c = infos[num].c + infos[num].w;

            if(arr[r][c] == 2) {
                block = true;
                break;
            }

            if(position[r][c] != 0) {
                another.add(position[r][c]);
            }
        }

        // 오른쪽 칸에 벽이 있는 경우
        if(block) {
            return false;
        }

        // 오른쪽 칸에 있는 다른 기사들을 이동시키지 못한 경우
        for(Integer i : another) {
            if(!right(i, depth + 1)) {
                return false;
            }
        }

        target.offer(new int[] {depth, num});

        return true;
    }

    private static void moveRight(int num) {
        for(int h = 0; h < infos[num].h; h++) {
            position[infos[num].r + h][infos[num].c] = 0;
            position[infos[num].r + h][infos[num].c + infos[num].w] = num;
        }
        infos[num].c = infos[num].c + 1;
    }

    private static boolean down(int num, int depth) { // num번 기사를 아래로 밀 수 있는가?
        // 맨 아래 칸인 경우
        if(infos[num].r + infos[num].h == L) {
            return false;
        }

        // 아래 칸 탐색
        boolean block = false; // 벽 존재 여부
        Set<Integer> another = new HashSet<>(); // 다른 기사 존재 여부
        for(int w = 0; w < infos[num].w; w++) {
            int r = infos[num].r + infos[num].h, c = infos[num].c + w;

            if(arr[r][c] == 2) {
                block = true;
                break;
            }

            if(position[r][c] != 0) {
                another.add(position[r][c]);
            }
        }

        // 아래 칸에 벽이 있는 경우
        if(block) {
            return false;
        }

        // 아래 칸에 있는 다른 기사들을 이동시키지 못한 경우
        for(Integer i : another) {
            if(!down(i, depth + 1)) {
                return false;
            }
        }

        target.offer(new int[] {depth, num});

        return true;
    }

    private static void moveDown(int num) {
        for(int w = 0; w < infos[num].w; w++) {
            position[infos[num].r][infos[num].c + w] = 0;
            position[infos[num].r + infos[num].h][infos[num].c + w] = num;
        }
        infos[num].r = infos[num].r + 1;
    }

    private static boolean left(int num, int depth) { // num번 기사를 왼쪽으로 밀 수 있는가?
        // 맨 왼쪽 칸인 경우
        if(infos[num].c == 0) {
            return false;
        }

        // 왼쪽 칸 탐색
        boolean block = false; // 벽 존재 여부
        Set<Integer> another = new HashSet<>(); // 다른 기사 존재 여부
        for(int h = 0; h < infos[num].h; h++) {
            int r = infos[num].r + h, c = infos[num].c - 1;

            if(arr[r][c] == 2) {
                block = true;
                break;
            }

            if(position[r][c] != 0) {
                another.add(position[r][c]);
            }
        }

        // 왼쪽 칸에 벽이 있는 경우
        if(block) {
            return false;
        }

        // 왼쪽 칸에 있는 다른 기사들을 이동시키지 못한 경우
        for(Integer i : another) {
            if(!left(i, depth + 1)) {
                return false;
            }
        }

        target.offer(new int[] {depth, num});

        return true;
    }

    private static void moveLeft(int num) {
        for(int h = 0; h < infos[num].h; h++) {
            position[infos[num].r + h][infos[num].c + infos[num].w - 1] = 0;
            position[infos[num].r + h][infos[num].c - 1] = num;
        }
        infos[num].c = infos[num].c - 1;
    }
}