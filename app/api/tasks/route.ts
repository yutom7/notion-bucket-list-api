import { Client } from '@notionhq/client';
import { NextResponse } from 'next/server';

// ★ キャッシュを徹底的に無効化する設定
export const dynamic = 'force-dynamic';
export const revalidate = 0;
export const fetchCache = 'force-no-store';

const notion = new Client({ auth: process.env.NOTION_API_KEY });

// ★ここにタスク管理のIDが入っているか確認してください！
const DATABASE_ID = "YOUR_DATABASE_ID_HERE";

export async function GET() {
  try {
    const response = await notion.databases.query({
      database_id: DATABASE_ID,
      filter: {
        property: "今日やる",
        checkbox: {
          equals: true,
        },
      },
      sorts: [
        {
          timestamp: "created_time",
          direction: "ascending",
        },
      ],
    });

    const tasks = response.results.map((page: any) => {
      const title = page.properties['タスク']?.title?.[0]?.plain_text || "No Title";
      const genre = page.properties['ジャンル']?.select?.name || "未分類";
      const status = page.properties['ステータス']?.status?.name || "未着手";

      return {
        id: page.id,
        title: title,
        genre: genre,
        status: status
      };
    });

    // ★ レスポンス自体にも「キャッシュ禁止」の焼き印を押して返す
    return NextResponse.json(tasks, {
      headers: {
        'Cache-Control': 'no-store, no-cache, must-revalidate, proxy-revalidate',
        'Pragma': 'no-cache',
        'Expires': '0',
      },
    });

  } catch (error) {
    console.error(error);
    return NextResponse.json(
      { error: 'Failed to fetch tasks' },
      {
        status: 500,
        headers: {
          'Cache-Control': 'no-store, no-cache', // エラー時もキャッシュしない
        }
      }
    );
  }
}