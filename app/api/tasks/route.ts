import { Client } from '@notionhq/client';
import { NextResponse } from 'next/server';

const notion = new Client({ auth: process.env.NOTION_API_KEY });
const TASKS_DATABASE_ID = process.env.TASKS_DATABASE_ID;

export async function GET() {
  try {
    const response = await notion.databases.query({
      database_id: TASKS_DATABASE_ID!,
      filter: {
        property: "今日やる",
        checkbox: {
          equals: true,
        },
      },
      sorts: [
        {
          // ★ここを修正！「作成日時」という列がなくても動くようにします
          timestamp: "created_time",
          direction: "descending", // 新しい順（昇順なら "ascending"）
        },
      ],
    });

    const tasks = response.results.map((page: any) => {
      return {
        id: page.id,
        title: page.properties["タスク"]?.title?.[0]?.plain_text || "無題",
        // ★Notionをセレクトに戻すなら、このままでOK！
        genre: page.properties["ジャンル"]?.select?.name || "未分類",
        status: page.properties["ステータス"]?.status?.name || "未設定",
      };
    });

    return NextResponse.json(tasks);
  } catch (error) {
    console.error(error);
    return NextResponse.json({ error: 'Failed to fetch tasks' }, { status: 500 });
  }
}