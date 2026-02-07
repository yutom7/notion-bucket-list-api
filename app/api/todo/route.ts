import { Client } from '@notionhq/client';
import { NextResponse } from 'next/server';

const notion = new Client({ auth: process.env.NOTION_API_KEY });
const DATABASE_ID = process.env.DATABASE_ID;

export async function GET() {
    try {
        const response = await notion.databases.query({
            database_id: DATABASE_ID!,
            filter: {
                property: "ステータス", // スクリーンショット通り
                status: {
                    equals: "未完了", // スクリーンショット通り
                },
            },
            sorts: [
                {
                    property: "期限", // スクリーンショット通り
                    direction: "ascending",
                },
            ],
        });

        const tasks = response.results.map((page: any) => ({
            id: page.id,
            title: page.properties["やりたいことリスト"].title[0]?.plain_text || "無題", // ここを修正！
            deadline: page.properties["期限"].date?.start || "期限なし",
        }));

        return NextResponse.json(tasks);
    } catch (error) {
        console.error(error);
        return NextResponse.json({ error: 'Failed to fetch' }, { status: 500 });
    }
}