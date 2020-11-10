export const searchRouter = 
{
	
	meta: {
		requireAuth: true,
		permit: 1,
		title:'searchCenter'
	},
	path: '/searchcenter',
	component: () => import('@/views/search/FullTextSearch.vue'),
	name: '搜索中心',
	children: [
		{
			meta: {
				requireAuth: true,
				permit: 1,
				title:'fullTextSearch'
			},
			path: '/search/fulltextsearch',
			name: '全文搜索',
			component: () => import('@/views/search/FullTextSearch.vue')
		},
	]

}
