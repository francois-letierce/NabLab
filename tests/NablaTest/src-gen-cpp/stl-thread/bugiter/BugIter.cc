#include "bugiter/BugIter.h"

using namespace nablalib;


/******************** Options definition ********************/

void
BugIter::Options::jsonInit(const rapidjson::Value::ConstObject& d)
{
	// maxTime
	if (d.HasMember("maxTime"))
	{
		const rapidjson::Value& valueof_maxTime = d["maxTime"];
		assert(valueof_maxTime.IsDouble());
		maxTime = valueof_maxTime.GetDouble();
	}
	else
		maxTime = 0.1;
	// maxIter
	if (d.HasMember("maxIter"))
	{
		const rapidjson::Value& valueof_maxIter = d["maxIter"];
		assert(valueof_maxIter.IsInt());
		maxIter = valueof_maxIter.GetInt();
	}
	else
		maxIter = 500;
	// maxIterK
	if (d.HasMember("maxIterK"))
	{
		const rapidjson::Value& valueof_maxIterK = d["maxIterK"];
		assert(valueof_maxIterK.IsInt());
		maxIterK = valueof_maxIterK.GetInt();
	}
	else
		maxIterK = 500;
	// maxIterL
	if (d.HasMember("maxIterL"))
	{
		const rapidjson::Value& valueof_maxIterL = d["maxIterL"];
		assert(valueof_maxIterL.IsInt());
		maxIterL = valueof_maxIterL.GetInt();
	}
	else
		maxIterL = 500;
	// deltat
	if (d.HasMember("deltat"))
	{
		const rapidjson::Value& valueof_deltat = d["deltat"];
		assert(valueof_deltat.IsDouble());
		deltat = valueof_deltat.GetDouble();
	}
	else
		deltat = 1.0;
}

/******************** Module definition ********************/

BugIter::BugIter(CartesianMesh2D* aMesh, const Options& aOptions)
: mesh(aMesh)
, nbCells(mesh->getNbCells())
, nbNodes(mesh->getNbNodes())
, options(aOptions)
, t_n(0.0)
, t_nplus1(0.0)
, X(nbNodes)
, u_n(nbCells)
, u_nplus1(nbCells)
, v_n(nbCells)
, v_nplus1(nbCells)
, v_nplus1_k(nbCells)
, v_nplus1_kplus1(nbCells)
, v_nplus1_k0(nbCells)
, w_n(nbCells)
, w_nplus1(nbCells)
, w_nplus1_l(nbCells)
, w_nplus1_lplus1(nbCells)
, w_nplus1_l0(nbCells)
{
	// Copy node coordinates
	const auto& gNodes = mesh->getGeometry()->getNodes();
	for (size_t rNodes=0; rNodes<nbNodes; rNodes++)
	{
		X[rNodes][0] = gNodes[rNodes][0];
		X[rNodes][1] = gNodes[rNodes][1];
	}
}

BugIter::~BugIter()
{
}

/**
 * Job ComputeTn called @1.0 in executeTimeLoopN method.
 * In variables: deltat, t_n
 * Out variables: t_nplus1
 */
void BugIter::computeTn() noexcept
{
	t_nplus1 = t_n + options.deltat;
}

/**
 * Job IniV called @1.0 in executeTimeLoopN method.
 * In variables: u_n
 * Out variables: v_nplus1_k0
 */
void BugIter::iniV() noexcept
{
	parallel::parallel_exec(nbCells, [&](const size_t& cCells)
	{
		v_nplus1_k0[cCells] = u_n[cCells] + 1;
	});
}

/**
 * Job InitU called @1.0 in simulate method.
 * In variables: 
 * Out variables: u_n
 */
void BugIter::initU() noexcept
{
	parallel::parallel_exec(nbCells, [&](const size_t& cCells)
	{
		u_n[cCells] = 0.0;
	});
}

/**
 * Job UpdateV called @1.0 in executeTimeLoopK method.
 * In variables: v_nplus1_k
 * Out variables: v_nplus1_kplus1
 */
void BugIter::updateV() noexcept
{
	parallel::parallel_exec(nbCells, [&](const size_t& cCells)
	{
		v_nplus1_kplus1[cCells] = v_nplus1_k[cCells] + 1.5;
	});
}

/**
 * Job UpdateW called @1.0 in executeTimeLoopL method.
 * In variables: w_nplus1_l
 * Out variables: w_nplus1_lplus1
 */
void BugIter::updateW() noexcept
{
	parallel::parallel_exec(nbCells, [&](const size_t& cCells)
	{
		w_nplus1_lplus1[cCells] = w_nplus1_l[cCells] + 2.5;
	});
}

/**
 * Job ExecuteTimeLoopN called @2.0 in simulate method.
 * In variables: deltat, t_n, u_n, v_nplus1, v_nplus1_k, v_nplus1_k0, v_nplus1_kplus1, w_nplus1, w_nplus1_l, w_nplus1_l0, w_nplus1_lplus1
 * Out variables: t_nplus1, u_nplus1, v_nplus1, v_nplus1_k, v_nplus1_k0, v_nplus1_kplus1, w_nplus1, w_nplus1_l, w_nplus1_l0, w_nplus1_lplus1
 */
void BugIter::executeTimeLoopN() noexcept
{
	n = 0;
	bool continueLoop = true;
	do
	{
		globalTimer.start();
		cpuTimer.start();
		n++;
		if (n!=1)
			std::cout << "[" << __CYAN__ << __BOLD__ << setw(3) << n << __RESET__ "] t = " << __BOLD__
				<< setiosflags(std::ios::scientific) << setprecision(8) << setw(16) << t_n << __RESET__;
	
		computeTn(); // @1.0
		iniV(); // @1.0
		setUpTimeLoopK(); // @2.0
		executeTimeLoopK(); // @3.0
		tearDownTimeLoopK(); // @4.0
		iniW(); // @5.0
		setUpTimeLoopL(); // @6.0
		executeTimeLoopL(); // @7.0
		tearDownTimeLoopL(); // @8.0
		updateU(); // @9.0
		
	
		// Evaluate loop condition with variables at time n
		continueLoop = (t_nplus1 < options.maxTime && n + 1 < options.maxIter);
	
		if (continueLoop)
		{
			// Switch variables to prepare next iteration
			std::swap(t_nplus1, t_n);
			std::swap(u_nplus1, u_n);
			std::swap(v_nplus1, v_n);
			std::swap(w_nplus1, w_n);
		}
	
		cpuTimer.stop();
		globalTimer.stop();
	
		// Timers display
			std::cout << " {CPU: " << __BLUE__ << cpuTimer.print(true) << __RESET__ ", IO: " << __RED__ << "none" << __RESET__ << "} ";
		
		// Progress
		std::cout << utils::progress_bar(n, options.maxIter, t_n, options.maxTime, 25);
		std::cout << __BOLD__ << __CYAN__ << utils::Timer::print(
			utils::eta(n, options.maxIter, t_n, options.maxTime, options.deltat, globalTimer), true)
			<< __RESET__ << "\r";
		std::cout.flush();
	
		cpuTimer.reset();
		ioTimer.reset();
	} while (continueLoop);
}

/**
 * Job SetUpTimeLoopK called @2.0 in executeTimeLoopN method.
 * In variables: v_nplus1_k0
 * Out variables: v_nplus1_k
 */
void BugIter::setUpTimeLoopK() noexcept
{
	for (size_t i1(0) ; i1<v_nplus1_k.size() ; i1++)
		v_nplus1_k[i1] = v_nplus1_k0[i1];
}

/**
 * Job ExecuteTimeLoopK called @3.0 in executeTimeLoopN method.
 * In variables: v_nplus1_k
 * Out variables: v_nplus1_kplus1
 */
void BugIter::executeTimeLoopK() noexcept
{
	k = 0;
	bool continueLoop = true;
	do
	{
		k++;
	
		updateV(); // @1.0
		
	
		// Evaluate loop condition with variables at time n
		continueLoop = (k + 1 < options.maxIterK);
	
		if (continueLoop)
		{
			// Switch variables to prepare next iteration
			std::swap(v_nplus1_kplus1, v_nplus1_k);
		}
	
	
	} while (continueLoop);
}

/**
 * Job TearDownTimeLoopK called @4.0 in executeTimeLoopN method.
 * In variables: v_nplus1_kplus1
 * Out variables: v_nplus1
 */
void BugIter::tearDownTimeLoopK() noexcept
{
	for (size_t i1(0) ; i1<v_nplus1.size() ; i1++)
		v_nplus1[i1] = v_nplus1_kplus1[i1];
}

/**
 * Job IniW called @5.0 in executeTimeLoopN method.
 * In variables: v_nplus1
 * Out variables: w_nplus1_l0
 */
void BugIter::iniW() noexcept
{
	parallel::parallel_exec(nbCells, [&](const size_t& cCells)
	{
		w_nplus1_l0[cCells] = v_nplus1[cCells];
	});
}

/**
 * Job SetUpTimeLoopL called @6.0 in executeTimeLoopN method.
 * In variables: w_nplus1_l0
 * Out variables: w_nplus1_l
 */
void BugIter::setUpTimeLoopL() noexcept
{
	for (size_t i1(0) ; i1<w_nplus1_l.size() ; i1++)
		w_nplus1_l[i1] = w_nplus1_l0[i1];
}

/**
 * Job ExecuteTimeLoopL called @7.0 in executeTimeLoopN method.
 * In variables: w_nplus1_l
 * Out variables: w_nplus1_lplus1
 */
void BugIter::executeTimeLoopL() noexcept
{
	l = 0;
	bool continueLoop = true;
	do
	{
		l++;
	
		updateW(); // @1.0
		
	
		// Evaluate loop condition with variables at time n
		continueLoop = (l + 1 < options.maxIterL);
	
		if (continueLoop)
		{
			// Switch variables to prepare next iteration
			std::swap(w_nplus1_lplus1, w_nplus1_l);
		}
	
	
	} while (continueLoop);
}

/**
 * Job TearDownTimeLoopL called @8.0 in executeTimeLoopN method.
 * In variables: w_nplus1_lplus1
 * Out variables: w_nplus1
 */
void BugIter::tearDownTimeLoopL() noexcept
{
	for (size_t i1(0) ; i1<w_nplus1.size() ; i1++)
		w_nplus1[i1] = w_nplus1_lplus1[i1];
}

/**
 * Job UpdateU called @9.0 in executeTimeLoopN method.
 * In variables: w_nplus1
 * Out variables: u_nplus1
 */
void BugIter::updateU() noexcept
{
	parallel::parallel_exec(nbCells, [&](const size_t& cCells)
	{
		u_nplus1[cCells] = w_nplus1[cCells];
	});
}

void BugIter::simulate()
{
	std::cout << "\n" << __BLUE_BKG__ << __YELLOW__ << __BOLD__ <<"\tStarting BugIter ..." << __RESET__ << "\n\n";
	
	std::cout << "[" << __GREEN__ << "TOPOLOGY" << __RESET__ << "]  HWLOC unavailable cannot get topological informations" << std::endl;
	
	std::cout << "[" << __GREEN__ << "OUTPUT" << __RESET__ << "]    " << __BOLD__ << "Disabled" << __RESET__ << std::endl;

	initU(); // @1.0
	executeTimeLoopN(); // @2.0
	
	std::cout << __YELLOW__ << "\n\tDone ! Took " << __MAGENTA__ << __BOLD__ << globalTimer.print() << __RESET__ << std::endl;
}

/******************** Module definition ********************/

int main(int argc, char* argv[]) 
{
	string dataFile;
	
	if (argc == 2)
	{
		dataFile = argv[1];
	}
	else
	{
		std::cerr << "[ERROR] Wrong number of arguments. Expecting 1 arg: dataFile." << std::endl;
		std::cerr << "(BugIterDefault.json)" << std::endl;
		return -1;
	}
	
	// read json dataFile
	ifstream ifs(dataFile);
	rapidjson::IStreamWrapper isw(ifs);
	rapidjson::Document d;
	d.ParseStream(isw);
	assert(d.IsObject());
	
	// mesh
	assert(d.HasMember("mesh"));
	const rapidjson::Value& valueof_mesh = d["mesh"];
	assert(valueof_mesh.IsObject());
	CartesianMesh2DFactory meshFactory;
	meshFactory.jsonInit(valueof_mesh.GetObject());
	CartesianMesh2D* mesh = meshFactory.create();
	
	// options
	BugIter::Options options;
	assert(d.HasMember("options"));
	const rapidjson::Value& valueof_options = d["options"];
	assert(valueof_options.IsObject());
	options.jsonInit(valueof_options.GetObject());
	
	// simulator must be a pointer if there is a finalize at the end (Kokkos, omp...)
	auto simulator = new BugIter(mesh, options);
	simulator->simulate();
	
	// simulator must be deleted before calling finalize
	delete simulator;
	delete mesh;
	return 0;
}
