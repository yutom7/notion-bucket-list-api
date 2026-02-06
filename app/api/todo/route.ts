import { Client } from '@notionhq/client';
import { NextResponse } from 'next/server';

// Notionクライアントの初期化
const notion = new Client({ auth: process.env.NOTION_API_KEY });
const DATABASE_ID = process.env.DATABASE_ID;

export async function GET() {
    try {
        const response = await notion.databases.query({
            database_id: DATABASE_ID!,
            filter: {
                property: "ステータス", // あなたのDBの「未完了」を示す列名に合わせてください
                status: {
                    does_not_equal: "Done",
                },
            },
            sorts: [
                {
                    property: "期限", // 期限が近い順
                    direction: "ascending",
                },
            ],
            page_size: 3, // 上位3件のみ
        });

        const tasks = response.results.map((page: any) => ({
            id: page.id,
            title: page.properties.名前.title[0]?.plain_text || "無題",
            deadline: page.properties.期限.date?.start || "期限なし",
        }));

        return NextResponse.json(tasks);
    } catch (error) {
        return NextResponse.json({ error: 'Failed to fetch' }, { status: 500 });
    }
}